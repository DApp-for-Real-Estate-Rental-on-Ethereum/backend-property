package ma.fstt.propertyservice.service;

import ma.fstt.propertyservice.model.Property;
import ma.fstt.propertyservice.repository.PropertyImageRepository;
import ma.fstt.propertyservice.repository.PropertyRepository;
import ma.fstt.propertyservice.repository.VerificationRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PricingServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private PropertyImageRepository propertyImageRepository;

    @Mock
    private VerificationRequestRepository verificationRequestRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(pricingService, "bookingServiceUrl", "http://booking-service");
        ReflectionTestUtils.setField(pricingService, "userServiceUrl", "http://user-service");
        ReflectionTestUtils.setField(pricingService, "pricingApiUrl", "http://pricing-api");
        ReflectionTestUtils.setField(pricingService, "restTemplate", restTemplate);
    }

    @Test
    void testPredictPrice_fetchesBookingCount() {
        // Arrange
        String propertyId = "123";
        Property property = new Property();
        property.setId(propertyId);
        property.setDailyPrice(100.0);

        when(propertyRepository.findByIdWithDetails(propertyId)).thenReturn(Optional.of(property));
        when(verificationRequestRepository.findByProperty(property)).thenReturn(Collections.emptyList());

        // Mock booking service response
        List<Map<String, Object>> bookings = List.of(Map.of("id", 1), Map.of("id", 2));
        when(restTemplate.getForObject(eq("http://booking-service/api/bookings/confirmed/property/" + propertyId),
                eq(List.class)))
                .thenReturn(bookings);

        // Mock pricing API response
        Map<String, Object> pricingResponse = Map.of(
                "predicted_price_mad", 120.0,
                "predicted_price_usd", 12.0);

        // Use any() for HttpEntity matcher to be safe
        when(restTemplate.postForObject(eq("http://pricing-api/predict"), any(), eq(Map.class)))
                .thenReturn(pricingResponse);

        // Act
        try {
            pricingService.predictPrice(propertyId, LocalDate.now(), LocalDate.now().plusDays(3));
        } catch (Exception e) {
            // we ignore exceptions here, we just want to verify interactions
        }

        // Assert
        verify(restTemplate).getForObject("http://booking-service/api/bookings/confirmed/property/123", List.class);
    }
}
