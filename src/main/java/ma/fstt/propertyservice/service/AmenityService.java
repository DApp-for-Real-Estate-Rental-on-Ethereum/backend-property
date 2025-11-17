package ma.fstt.propertyservice.service;

import ma.fstt.propertyservice.dto.requests.CreateAmenityRequest;
import ma.fstt.propertyservice.model.Amenity;
import ma.fstt.propertyservice.model.AmenityCategory;
import ma.fstt.propertyservice.repository.AmenityCategoryRepository;
import ma.fstt.propertyservice.repository.AmenityRepository;
import ma.fstt.propertyservice.service.storageService.AmenityStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AmenityService {
    private final AmenityRepository amenityRepository;
    private final AmenityCategoryRepository amenityCategoryRepository;
    private final AmenityStorageService amenityStorageService;


    public AmenityService(AmenityRepository amenityRepository, AmenityCategoryRepository amenityCategoryRepository, AmenityStorageService amenityStorageService) {
        this.amenityRepository = amenityRepository;
        this.amenityCategoryRepository = amenityCategoryRepository;
        this.amenityStorageService = amenityStorageService;
    }

    @Transactional
    public Amenity createAmenity(CreateAmenityRequest input, MultipartFile amenityIcon) {
        AmenityCategory category;
        if (input.getCategoryId() == null) {
            category = createAmenityCategory(input.getAmenityCategoryTitle());
        } else {
            category = amenityCategoryRepository.findById(input.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Amenity category not found"));
        }

        Amenity amenity = Amenity.builder()
                .name(input.getAmenityName())
                .category(category)
                .build();

        Amenity savedAmenity = amenityRepository.save(amenity);

        savedAmenity.setIcon(amenityStorageService.storeAmenity(amenityIcon, savedAmenity));

        return amenityRepository.save(savedAmenity);
    }

    @Transactional
    AmenityCategory createAmenityCategory(String title) {

        if(title == null ||title.isBlank())
        {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        AmenityCategory amenityCategory = new AmenityCategory();
        amenityCategory.setTitle(title);

        AmenityCategory savedCategory = amenityCategoryRepository.save(amenityCategory);

        return amenityCategoryRepository.save(savedCategory);
    }
}
