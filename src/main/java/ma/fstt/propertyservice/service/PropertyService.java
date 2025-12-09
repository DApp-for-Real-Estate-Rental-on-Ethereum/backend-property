package ma.fstt.propertyservice.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.*;
import ma.fstt.propertyservice.enums.PropertyStatusEnum;
import ma.fstt.propertyservice.exception.*;
import ma.fstt.propertyservice.model.*;
import ma.fstt.propertyservice.repository.*;
import ma.fstt.propertyservice.service.storageService.ProperyFileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class PropertyService {
    @PersistenceContext
    private EntityManager entityManager;
    
    private final ProperyFileService propertyFileService;
    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final UserProfileService userProfileService;
    private final PropertyTypeRepository propertyTypeRepository;
    private final SuspensionRepository  suspensionRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;

    public String createProperty(CreatePropertyRequest input, String hostID, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Property must include at least 5 images");
        }
        int imageCount = files.size();
        if (imageCount < 5) {
            throw new IllegalArgumentException("Property must include at least 5 images. Currently provided: " + imageCount);
        }
        if (imageCount > 10) {
            throw new IllegalArgumentException("Property can include at most 10 images. Currently provided: " + imageCount);
        }
        Property property = input.createProperty();
        property.setUserId(hostID);
        
        String propertyId = "prop-" + String.format("%03d", propertyRepository.count() + 1);
        property.setId(propertyId);

        property.setPrice(property.getDailyPrice());

        property.setAmenities(amenitiesCheck(input.getAmenities()));
        PropertyType type = propertyTypeRepository.findById(input.getTypeId())
                .orElseThrow(() -> new RuntimeException("Property type not found"));
        property.setType(type);
        property.setStatus(PropertyStatusEnum.DRAFT);
        Property savedProperty = propertyRepository.save(property);
        
        try {
            updateUserRoleToHost(hostID);
        } catch (Exception e) {
        }
        
        Set<PropertyImage> propertyImages = propertyFileService.storePropertyImages(files, savedProperty, input.getCoverImageName());
        propertyImageRepository.saveAll(propertyImages);
        return savedProperty.getId();
    }

    private void updateUserRoleToHost(String userId) {
        try {
            String url = userServiceUrl + "/api/v1/users/" + userId + "/become-host";
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user role", e);
        }
    }

    @Transactional
    public void updateProperty(String id, UpdatePropertyRequest input, String hostID) {
        Property property = propertyRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        
        if(!property.getUserId().equals(hostID)){
            throw new UserNotPermitedException();
        }
        
        if (input.getTitle() != null && !input.getTitle().trim().isEmpty()) {
            property.setTitle(input.getTitle().trim());
        }
        
        if (input.getDescription() != null) {
            property.setDescription(input.getDescription().trim());
        }
        
        if (input.getDailyPrice() != null) {
            property.setDailyPrice(input.getDailyPrice());
            property.setPrice(input.getDailyPrice()); // Also update price field
        }
        
        if (input.getCapacity() != null) {
            property.setCapacity(input.getCapacity());
        }
        
        if (input.getNumberOfBedrooms() != null) {
            property.setNumberOfBedrooms(input.getNumberOfBedrooms());
        }
        
        if (input.getNumberOfBeds() != null) {
            property.setNumberOfBeds(input.getNumberOfBeds());
        }
        
        if (input.getNumberOfBathrooms() != null) {
            property.setNumberOfBathrooms(input.getNumberOfBathrooms());
        }
        
        if (input.getTypeId() != null) {
            PropertyType type = propertyTypeRepository.findById(input.getTypeId())
                    .orElseThrow(() -> new RuntimeException("Property type not found"));
            property.setType(type);
        }
        
        if (input.getAddress() != null || input.getCity() != null || input.getCountry() != null || input.getZipCode() != null) {
            Address address = property.getAddress();
            if (address == null) {
                address = new Address();
                property.setAddress(address);
            }
            if (input.getAddress() != null) {
                address.setAddress(input.getAddress().trim());
            }
            if (input.getCity() != null) {
                address.setCity(input.getCity().trim());
            }
            if (input.getCountry() != null) {
                address.setCountry(input.getCountry().trim());
            }
            if (input.getZipCode() != null && !input.getZipCode().trim().isEmpty()) {
                try {
                    address.setZipCode(Integer.parseInt(input.getZipCode().trim()));
                } catch (NumberFormatException e) {
                }
            }
        }
        
        if (input.getAmenities() != null) {
            property.setAmenities(amenitiesCheck(input.getAmenities()));
        }
        
        Property savedProperty = propertyRepository.saveAndFlush(property);
        
        entityManager.detach(savedProperty);
        
        Property verifiedProperty = propertyRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Property not found after update"));
    }

    public void deleteProperty(String id, String hostID)
    {
        Property property = getPropertyById(id);
        if(!property.getUserId().equals(hostID)){
            throw new UserNotPermitedException();
        }
        property.setStatus(PropertyStatusEnum.HIDDEN);
        propertyRepository.save(property);
    }

    public void deletePropertyImage(Long id, String hostID) {
        PropertyImage propertyImage = propertyImageRepository.findById(id).orElseThrow(() -> new PropertyImageNotFoundException());
        Property property = propertyImage.getProperty();
        if(!property.getUserId().equals(hostID)){
            throw new UserNotPermitedException();
        }
        int currentImageCount = property.getPropertyImages().size();
        if(currentImageCount <= 5){
            throw new PropertyImagesDoNotExceedRequiredMinimumException();
        }
        property.getPropertyImages().remove(propertyImage);
        if (propertyImage.getCover()) {
            boolean first = true;
            for (PropertyImage i : property.getPropertyImages()) {
                i.setCover(first);
                propertyImageRepository.save(i);
                first = false;
            }
        }
        propertyFileService.deletePropertyImage(propertyImage);
        propertyImageRepository.delete(propertyImage);
    }

    public boolean hasAmenity(Long amenityId)
    {
        return propertyRepository.countByAmenityId(amenityId) > 0;
    }

    private Set<Amenity> amenitiesCheck(Set<Amenity> inputAmenities) {
        Set<Amenity> amenities = new HashSet<>();
        inputAmenities.forEach(amenity -> {
            if (amenityRepository.existsById(amenity.getId())) {
                amenities.add(amenity);
            }else {
                throw new AmenityNotFoundException();
            }
        });
        return amenities;
    }

    public void approveProperty(String id, ApprovePropertyRequest input)
    {
        Property property = getPropertyById(id);
        PropertyStatusEnum newStatus = input.getIsApproved() ? PropertyStatusEnum.APPROVED : PropertyStatusEnum.DISAPPROVED;
        updatePropertyStatus(property, newStatus);
    }

    public void updatePropertyStatus(Property property, PropertyStatusEnum status) {
        property.setStatus(status);
        propertyRepository.save(property);
    }

    public void hideProperty(String id, String hostId, HidePropertyRequest input) {
        Property property = getPropertyById(id);
        if(!property.getUserId().equals(hostId)){
            throw new UserNotPermitedException();
        }
        PropertyStatusEnum newStatus = input.getIsHidden() ? PropertyStatusEnum.HIDDEN : PropertyStatusEnum.APPROVED;
        updatePropertyStatus(property, newStatus);
    }

    @Transactional(readOnly = true)
    public Property getPropertyById(String id)
    {
        Property property = null;
        
        try {
            property = propertyRepository.findByIdWithDetails(id).orElse(null);
        } catch (Exception e) {
        }
        
        if (property == null) {
            property = propertyRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
        }
        
        if (property.getDepositAmount() == null) {
            property.setDepositAmount(0.0);
        }
        
        try {
            Hibernate.initialize(property.getType());
        } catch (Exception e) {
        }
        
        try {
            Hibernate.initialize(property.getAddress());
        } catch (Exception e) {
        }
        
        try {
            Hibernate.initialize(property.getPropertyImages());
        } catch (Exception e) {
        }
        
        try {
            Hibernate.initialize(property.getAmenities());
        } catch (Exception e) {
        }
        
        return property;
    }

    public List<Property> getAllApprovedProperties() {
        try {
            List<Property> properties = propertyRepository.findAllByStatus(PropertyStatusEnum.APPROVED);
            for (Property property : properties) {
                if (property.getDepositAmount() == null) {
                    property.setDepositAmount(0.0);
                }
            }
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch approved properties: " + e.getMessage(), e);
        }
    }

    public List<Property> getAllProperties() {
        try {
            List<Property> properties = propertyRepository.findAllWithDetails();
            for (Property property : properties) {
                if (property.getDepositAmount() == null) {
                    property.setDepositAmount(0.0);
                }
            }
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch all properties: " + e.getMessage(), e);
        }
    }

    public List<Property> getPropertiesByUserId(String userId) {
        try {
            List<Property> properties = propertyRepository.findAllByUserId(userId);
            for (Property property : properties) {
                if (property.getDepositAmount() == null) {
                    property.setDepositAmount(0.0);
                }
            }
            return properties;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch properties for user: " + e.getMessage(), e);
        }
    }

    public ma.fstt.propertyservice.dto.responses.PropertyBookingInfoDTO getPropertyBookingInfo(String propertyId) {
        Property property = getPropertyById(propertyId);
        
        Long ownerId = null;
        try {
            if (property.getUserId() != null && !property.getUserId().isEmpty()) {
                ownerId = Long.parseLong(property.getUserId());
            }
        } catch (NumberFormatException e) {
        }
        
        boolean discountEnabled = property.getDiscountPlan() != null && 
            (property.getDiscountPlan().getFiveDays() != null || 
             property.getDiscountPlan().getFifteenDays() != null || 
             property.getDiscountPlan().getOneMonth() != null);
        
        boolean isNegotiable = discountEnabled;
        
        Integer maxNegotiationPercent = discountEnabled ? 20 : null;
        
        return ma.fstt.propertyservice.dto.responses.PropertyBookingInfoDTO.builder()
                .id(property.getId())
                .ownerId(ownerId)
                .pricePerNight(java.math.BigDecimal.valueOf(property.getDailyPrice()))
                .isNegotiable(isNegotiable)
                .discountEnabled(discountEnabled)
                .maxNegotiationPercent(maxNegotiationPercent)
                .negotiationPercentage(property.getNegotiationPercentage())
                .build();
    }

    public void addNewPropertyImages(AddNewPropertyImagesRequest input, String userId, List<MultipartFile> newImages) {
        Property property =  propertyRepository.findById(input.getPropertyId())
                .orElseThrow(PropertyImageNotFoundException::new);
        if(!property.getUserId().equals(userId)){
            throw new UserNotPermitedException();
        }
        
        int currentImageCount = property.getPropertyImages().size();
        int newImageCount = newImages != null ? newImages.size() : 0;
        int totalImageCount = currentImageCount + newImageCount;
        
        if (totalImageCount > 10) {
            throw new IllegalArgumentException(
                String.format("Property can have at most 10 images. Currently has %d images, trying to add %d more. Maximum allowed: %d", 
                    currentImageCount, newImageCount, 10 - currentImageCount)
            );
        }
        
        boolean hasExistingCover = property.getPropertyImages()
                .stream()
                .anyMatch(PropertyImage::getCover);
        String coverParam = (hasExistingCover && input.getCoverImageName() == null)
                ? "__NO_COVER__"
                : input.getCoverImageName();
        Set<PropertyImage> newImagesSet =
                propertyFileService.storePropertyImages(newImages, property, coverParam);

        propertyImageRepository.saveAll(newImagesSet);
    }
    public void updatePropertyImage(Long id, String hostId) {
        PropertyImage newCover = propertyImageRepository.findById(id)
                .orElseThrow(PropertyImageNotFoundException::new);
        Property property = newCover.getProperty();
        if (!property.getUserId().equals(hostId)) {
            throw new UserNotPermitedException();
        }
        PropertyImage currentCover = property.getPropertyImages().stream()
                .filter(PropertyImage::getCover)
                .findFirst()
                .orElse(null);
        if (currentCover != null && currentCover.getId().equals(id)) {
            return;
        }
        if (currentCover != null) {
            currentCover.setCover(false);
            propertyImageRepository.save(currentCover);
        }
        newCover.setCover(true);
        propertyImageRepository.save(newCover);
    }

    public void suspendProperty(String id, SuspensionPropertyRequest input) {
        Property property = getPropertyById(id);
        if(!property.getStatus().equals(PropertyStatusEnum.APPROVED)){
            throw new PropertyCannotBeSuspendedException();
        }
        Suspension suspension = new Suspension();
        suspension.setActive(true);
        suspension.setCreatedAt(LocalDateTime.now());
        suspension.setReason(input.getReason());
        suspension.setProperty(property);
        suspensionRepository.save(suspension);
        property.setStatus(PropertyStatusEnum.SUSPENDED);
        propertyRepository.save(property);
    }

    public void revokeSuspension(String id) {
        Property property = getPropertyById(id);
        if (!property.getStatus().equals(PropertyStatusEnum.SUSPENDED)) {
            throw new PropertyIsNotSuspendedException();
        }
        Suspension suspension = suspensionRepository.findByPropertyAndActiveTrue(property).orElseThrow(() -> new RuntimeException("Active suspension not found"));

        suspension.setActive(false);
        suspensionRepository.save(suspension);

        property.setStatus(PropertyStatusEnum.APPROVED);
        propertyRepository.save(property);
    }

    public void submitForApproval(String id, String hostId) {
        Property property = getPropertyById(id);
        if(!property.getUserId().equals(hostId)){
            throw new UserNotPermitedException();
        }
        if (!property.getStatus().equals(PropertyStatusEnum.DRAFT) && !property.getStatus().equals(PropertyStatusEnum.DISAPPROVED)) {
            throw new RuntimeException("Property can only be submitted for approval from DRAFT or DISAPPROVED status");
        }
        updatePropertyStatus(property, PropertyStatusEnum.PENDING_APPROVAL);
    }

    public void cancelApprovalRequest(String id, String hostId) {
        Property property = getPropertyById(id);
        if(!property.getUserId().equals(hostId)){
            throw new UserNotPermitedException();
        }
        if (!property.getStatus().equals(PropertyStatusEnum.PENDING_APPROVAL)) {
            throw new RuntimeException("Property must be in PENDING_APPROVAL status to cancel the request");
        }
        updatePropertyStatus(property, PropertyStatusEnum.DRAFT);
    }


}
