package ma.fstt.propertyservice.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.CategoryUpdateRequest;
import ma.fstt.propertyservice.dto.requests.CreateAmenityRequest;
import ma.fstt.propertyservice.dto.requests.UpdateAmenityRequest;
import ma.fstt.propertyservice.exception.AmenityCategoryInUseException;
import ma.fstt.propertyservice.exception.AmenityCategoryNotFoundException;
import ma.fstt.propertyservice.exception.AmenityInUseException;
import ma.fstt.propertyservice.exception.AmenityNotFoundException;
import ma.fstt.propertyservice.model.Amenity;
import ma.fstt.propertyservice.model.AmenityCategory;
import ma.fstt.propertyservice.repository.AmenityCategoryRepository;
import ma.fstt.propertyservice.repository.AmenityRepository;
import ma.fstt.propertyservice.service.storageService.AmenityStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class AmenityService {
    private final AmenityRepository amenityRepository;
    private final PropertyService propertyService;
    private final AmenityCategoryRepository amenityCategoryRepository;
    private final AmenityStorageService amenityStorageService;

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

        savedAmenity.setIcon(amenityStorageService.storeAmenityImage(amenityIcon, savedAmenity));

        return amenityRepository.save(savedAmenity);
    }

    public void deleteAmenity(Long id) throws AmenityInUseException {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(() -> new AmenityNotFoundException());

        if(propertyService.hasAmenity(id))
        {
            throw new AmenityInUseException();
        }

        amenityStorageService.deleteAmenity(amenity);

        amenityRepository.deleteById(id);
    }

    public void deleteAmenityCategory(Long id) {
        AmenityCategory amenityCategory = amenityCategoryRepository.findById(id).orElseThrow(() -> new AmenityCategoryNotFoundException());

        if(amenityCategory.getAmenities().size() > 0){
            throw new AmenityCategoryInUseException();
        }

        amenityRepository.deleteById(id);
    }

    public void updateAmenity(Long id, UpdateAmenityRequest input, MultipartFile amenityIcon) {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(() -> new AmenityNotFoundException());

        if(input.getAmenityName() != null && !input.getAmenityName().isEmpty()) {
            amenity.setName(input.getAmenityName());
        }

        if(!amenityIcon.isEmpty()){
            String url = amenityStorageService.updateAmenityImage(amenityIcon, amenity);
            amenity.setIcon(url);
        }

        amenityRepository.save(amenity);
    }

    private AmenityCategory createAmenityCategory(String title) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }

        AmenityCategory amenityCategory = new AmenityCategory();
        amenityCategory.setTitle(title);

        AmenityCategory savedCategory = amenityCategoryRepository.save(amenityCategory);

        return amenityCategoryRepository.save(savedCategory);
    }

    public void updateAmenityCategory(Long id, CategoryUpdateRequest input) {
        AmenityCategory amenityCategory =amenityCategoryRepository.findById(id).orElseThrow(() -> new AmenityCategoryNotFoundException());
        amenityCategory.setTitle(input.getCategoryName());
        amenityCategoryRepository.save(amenityCategory);
    }
}
