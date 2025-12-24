package ma.fstt.propertyservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.fstt.propertyservice.dto.responses.PricingPredictionResponse;
import ma.fstt.propertyservice.model.Property;
import ma.fstt.propertyservice.model.VerificationRequest;
import ma.fstt.propertyservice.repository.PropertyImageRepository;
import ma.fstt.propertyservice.repository.PropertyRepository;
import ma.fstt.propertyservice.repository.VerificationRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PricingService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final VerificationRequestRepository verificationRequestRepository;
    // UserProfileService not directly used but kept for future enhancements
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${pricing.api.url:http://localhost:8000}")
    private String pricingApiUrl;

    @Value("${pricing.api.timeout:5000}")
    private int timeout;

    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;

    @Value("${booking.service.url:http://localhost:8083}")
    private String bookingServiceUrl;

    /**
     * Predict optimal price for a property based on booking dates.
     * Automatically extracts all features from database.
     */
    public PricingPredictionResponse predictPrice(String propertyId, LocalDate checkInDate, LocalDate checkOutDate) {
        try {
            // Load property with all relationships
            Property property = propertyRepository.findByIdWithDetails(propertyId)
                    .orElseThrow(() -> new RuntimeException("Property not found: " + propertyId));

            // Extract features from database
            Map<String, Object> features = extractFeatures(property, checkInDate, checkOutDate);

            // Call pricing API
            PricingPredictionResponse prediction = callPricingAPI(features);

            // Calculate comparison with current price
            Double currentPrice = property.getDailyPrice();
            if (currentPrice != null && prediction.getPredictedPriceMad() != null) {
                double difference = prediction.getPredictedPriceMad() - currentPrice;
                double differencePercent = (difference / currentPrice) * 100.0;

                prediction.setCurrentPriceMad(currentPrice);
                prediction.setPriceDifferencePercent(differencePercent);

                // Generate recommendation
                if (differencePercent > 5.0) {
                    prediction.setRecommendation("INCREASE");
                } else if (differencePercent < -5.0) {
                    prediction.setRecommendation("DECREASE");
                } else {
                    prediction.setRecommendation("MAINTAIN");
                }
            }

            log.info("Price prediction for property {}: {} MAD (current: {} MAD, diff: {}%)",
                    propertyId, prediction.getPredictedPriceMad(), currentPrice,
                    prediction.getPriceDifferencePercent());

            return prediction;

        } catch (Exception e) {
            log.error("Failed to predict price for property {}", propertyId, e);
            throw new RuntimeException("Failed to predict price: " + e.getMessage(), e);
        }
    }

    /**
     * Extract all features needed for pricing model from database.
     */
    private Map<String, Object> extractFeatures(Property property, LocalDate checkInDate, LocalDate checkOutDate) {
        Map<String, Object> features = new HashMap<>();

        // Direct features from property
        features.put("bedroom_count",
                property.getNumberOfBedrooms() != null ? property.getNumberOfBedrooms().doubleValue() : 1.0);
        features.put("bed_count", property.getNumberOfBeds() != null ? property.getNumberOfBeds().doubleValue() : 1.0);

        // City normalization (from address or default)
        String cityName = "casablanca"; // Default
        if (property.getAddress() != null && property.getAddress().getCity() != null) {
            cityName = property.getAddress().getCity();
        }
        String city = normalizeCity(cityName);
        features.put("city", city);

        // Image count (use property's loaded images if available)
        int imageCount = 0;
        if (property.getPropertyImages() != null) {
            imageCount = property.getPropertyImages().size();
        } else {
            // Fallback: query repository
            imageCount = propertyImageRepository.findAll().stream()
                    .filter(img -> img.getProperty() != null && property.getId().equals(img.getProperty().getId()))
                    .mapToInt(img -> 1)
                    .sum();
        }
        features.put("image_count", imageCount);

        // Badge count (verification requests approved)
        List<VerificationRequest> verifications = verificationRequestRepository.findByProperty(property);
        long badgeCount = verifications.stream()
                .filter(v -> v.getStatus() != null && "APPROVED".equalsIgnoreCase(v.getStatus().toString()))
                .count();
        features.put("badge_count", (int) badgeCount);

        // Stay length
        long stayLengthNights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        features.put("stay_length_nights", (int) stayLengthNights);

        // Discount rate (from property discount plans)
        double discountRate = calculateDiscountRate(property, (int) stayLengthNights);
        features.put("discount_rate", discountRate);

        // Season category
        String seasonCategory = getSeasonCategory(checkInDate);
        features.put("season_category", seasonCategory);

        // Rating value (proxy: use owner's rating from user-service)
        Double ownerRating = null;
        try {
            if (property.getUserId() != null) {
                ownerRating = getUserRatingFromUserService(property.getUserId());
            }
        } catch (Exception e) {
            log.warn("Could not fetch owner rating for user {}: {}", property.getUserId(), e.getMessage());
        }
        features.put("rating_value", ownerRating != null ? ownerRating : 4.0); // Default 4.0

        // Rating count (proxy: count bookings - need to call booking-service or use
        // property booking info)
        int ratingCount = getBookingCount(property.getId());
        features.put("rating_count", ratingCount);

        // Review density (proxy: calculate from rating_count and property age)
        // Simplified calculation
        double reviewDensity = calculateReviewDensity(ratingCount, property);
        features.put("review_density", reviewDensity);

        // Quality proxy (calculated from amenities, status, images, size)
        double qualityProxy = calculateQualityProxy(property);
        features.put("quality_proxy", qualityProxy);

        log.debug("Extracted features for property {}: {}", property.getId(), features);
        return features;
    }

    /**
     * Call the pricing API with extracted features.
     */
    private PricingPredictionResponse callPricingAPI(Map<String, Object> features) {
        try {
            String url = pricingApiUrl + "/predict";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(features, headers);

            Map<String, Object> response = restTemplate.postForObject(url, request, Map.class);

            if (response == null) {
                throw new RuntimeException("Empty response from pricing API");
            }

            return PricingPredictionResponse.builder()
                    .predictedPriceMad(getDouble(response, "predicted_price_mad"))
                    .predictedPriceUsd(getDouble(response, "predicted_price_usd"))
                    .confidenceIntervalLower(getDouble(response, "confidence_interval_lower"))
                    .confidenceIntervalUpper(getDouble(response, "confidence_interval_upper"))
                    .city(getString(response, "city"))
                    .season(getString(response, "season"))
                    .modelVersion(getString(response, "model_version"))
                    .predictionTimestamp(getString(response, "prediction_timestamp"))
                    .build();

        } catch (RestClientException e) {
            log.error("Error calling pricing API: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to call pricing API: " + e.getMessage(), e);
        }
    }

    /**
     * Normalize city name to match pricing model expectations.
     */
    private String normalizeCity(String city) {
        if (city == null) {
            return "casablanca";
        }

        String normalized = city.toLowerCase().trim();

        // Map common variations to model cities
        Map<String, String> cityMapping = Map.of(
                "casablanca", "casablanca",
                "marrakech", "marrakech",
                "marrakesh", "marrakech",
                "agadir", "agadir",
                "rabat", "rabat",
                "fes", "fes",
                "fez", "fes",
                "tangier", "tangier",
                "tanger", "tangier");

        return cityMapping.getOrDefault(normalized, "casablanca"); // Default fallback
    }

    /**
     * Get season category from check-in date.
     */
    private String getSeasonCategory(LocalDate checkInDate) {
        Month month = checkInDate.getMonth();

        if (month == Month.MARCH) {
            return "march";
        } else if (month == Month.APRIL) {
            return "april";
        } else if (month == Month.JUNE || month == Month.JULY || month == Month.AUGUST) {
            return "summer";
        } else {
            return "other";
        }
    }

    /**
     * Calculate discount rate based on property discount plans and stay length.
     */
    private double calculateDiscountRate(Property property, int stayLengthNights) {
        if (property.getDiscountPlan() == null) {
            return 0.0;
        }

        // Check discount plans
        if (stayLengthNights >= 30 && property.getDiscountPlan().getOneMonth() != null) {
            return property.getDiscountPlan().getOneMonth() / 100.0;
        } else if (stayLengthNights >= 15 && property.getDiscountPlan().getFifteenDays() != null) {
            return property.getDiscountPlan().getFifteenDays() / 100.0;
        } else if (stayLengthNights >= 5 && property.getDiscountPlan().getFiveDays() != null) {
            return property.getDiscountPlan().getFiveDays() / 100.0;
        }

        return 0.0;
    }

    /**
     * Calculate review density (reviews per month).
     */
    private double calculateReviewDensity(int ratingCount, Property property) {
        if (ratingCount == 0) {
            return 0.0;
        }

        // Simplified: assume property exists for at least 1 month
        // In production, calculate from property creation date
        int monthsActive = Math.max(1, ratingCount / 10); // Rough estimate
        return (double) ratingCount / monthsActive;
    }

    /**
     * Calculate quality proxy score (0.0 to 1.0).
     */
    private double calculateQualityProxy(Property property) {
        double score = 0.0;

        // Amenities (0-0.3)
        if (property.getAmenities() != null) {
            int amenityCount = property.getAmenities().size();
            score += Math.min(amenityCount / 20.0, 0.3);
        }

        // Property status (0-0.2)
        if (property.getStatus() != null) {
            if (property.getStatus().toString().equals("APPROVED")) {
                score += 0.2;
            } else if (property.getStatus().toString().equals("PENDING_APPROVAL")) {
                score += 0.1;
            }
        }

        // Property size (0-0.2)
        int bedrooms = property.getNumberOfBedrooms() != null ? property.getNumberOfBedrooms() : 1;
        int bathrooms = property.getNumberOfBathrooms() != null ? property.getNumberOfBathrooms() : 1;
        double sizeScore = (bedrooms + bathrooms) / 10.0;
        score += Math.min(sizeScore, 0.2);

        // Images (0-0.3)
        int imageCount = 0;
        if (property.getPropertyImages() != null) {
            imageCount = property.getPropertyImages().size();
        }
        score += Math.min(imageCount / 10.0, 0.3);

        return Math.min(score, 1.0);
    }

    // Helper methods for response parsing
    private Double getDouble(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null)
            return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get booking count from booking-service.
     */
    private int getBookingCount(String propertyId) {
        try {
            String url = bookingServiceUrl + "/api/bookings/confirmed/property/" + propertyId;
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> bookings = restTemplate.getForObject(url, List.class);
            if (bookings != null) {
                return bookings.size();
            }
        } catch (Exception e) {
            log.warn("Could not fetch booking count for property {}: {}", propertyId, e.getMessage());
        }
        return 0; // Default fallback
    }

    /**
     * Get user rating from user-service.
     */
    private Double getUserRatingFromUserService(String userId) {
        try {
            String url = userServiceUrl + "/api/v1/users/" + userId;
            @SuppressWarnings("unchecked")
            Map<String, Object> userResponse = restTemplate.getForObject(url, Map.class);

            if (userResponse != null && userResponse.containsKey("rating")) {
                Object rating = userResponse.get("rating");
                if (rating instanceof Number) {
                    return ((Number) rating).doubleValue();
                }
            }
        } catch (Exception e) {
            log.debug("Could not fetch user rating from user-service: {}", e.getMessage());
        }
        return null;
    }
}
