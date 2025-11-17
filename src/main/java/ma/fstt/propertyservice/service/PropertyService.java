package ma.fstt.propertyservice.service;

import ma.fstt.propertyservice.dto.requests.CreatePropertyRequest;
import ma.fstt.propertyservice.model.*;
import ma.fstt.propertyservice.repository.AmenityRepository;
import ma.fstt.propertyservice.repository.PropertyImageRepository;
import ma.fstt.propertyservice.repository.PropertyRepository;
import ma.fstt.propertyservice.service.storageService.ProperyFileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class PropertyService {
    private final ProperyFileService properyFileService;
    private final PropertyRepository propertyRepository;
    private final AmenityRepository amenityRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final UserService userService;

    public PropertyService(PropertyRepository propertyRepository,
                           ProperyFileService properyFileService,
                           AmenityRepository amenityRepository,
                           PropertyImageRepository propertyImageRepository, UserService userService) {
        this.propertyRepository = propertyRepository;
        this.properyFileService = properyFileService;
        this.amenityRepository = amenityRepository;
        this.propertyImageRepository = propertyImageRepository;
        this.userService = userService;
    }

    @Transactional
    public Property createProperty(CreatePropertyRequest input, String hostID, List<MultipartFile> files) {
        if (files.size() < 5) {
            throw new IllegalArgumentException("Should include at least 5 property images");
        }

        Property property = newPropertyBuilder(input, hostID);

        if(input.getDraft() || !userService.isProfileComplete(hostID)){
            property.setDraft(true);
        }

        Set<Amenity> amenities = new HashSet<>();
        input.getAmenities().forEach(amenity -> {
            if (amenityRepository.existsById(amenity.getId())) {
                amenities.add(amenity);
            }
        });

        property.setAmenities(amenities);

        Property finalProperty = propertyRepository.save(property);

        Set<PropertyImage> propertyImages = properyFileService.storePropertyImages(files, finalProperty, input.getCoverImageName());
        propertyImageRepository.saveAll(propertyImages);

        return finalProperty;
    }

    private Property newPropertyBuilder(CreatePropertyRequest input, String hostID) {
        Property property = new Property();
        property.setTitle(input.getTitle());
        property.setDescription(input.getDescription());
        property.setType(input.getType());
        property.setPrice(input.getPrice());
        property.setCapacity(input.getCapacity());
        property.setLatitude(input.getLatitude());
        property.setLongitude(input.getLongitude());
        property.setCountry(input.getCountry());
        property.setCity(input.getCity());
        property.setPostalCode(input.getPostalCode());
        property.setAddress(input.getAddress());
        property.setUserId(hostID);
        return property;
    }
}
