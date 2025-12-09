package ma.fstt.propertyservice.service.storageService;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.model.Amenity;
import ma.fstt.propertyservice.service.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AmenityStorageService {

    private final IStorageService storageService;

    @Value("${aws.amenities.folder}")
    private String folder;

    public String storeAmenityImage(MultipartFile amenityIcon, Amenity amenity) {

        String extension = StringUtils.getFilenameExtension(
                Objects.requireNonNullElse(amenityIcon.getOriginalFilename(), "")
        );

        if (extension == null) {
            throw new IllegalArgumentException("File must have an extension");
        }

        extension = extension.toLowerCase();

        if (!List.of("jpeg", "jpg", "png", "svg").contains(extension)) {
            throw new IllegalArgumentException("Allowed: JPG, JPEG, PNG, SVG");
        }

        String contentType = amenityIcon.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("Invalid MIME type");
        }

        if (!(contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/svg+xml"))) {
            throw new IllegalArgumentException("Invalid MIME type for image");
        }

        String fileName = generateSafeFileName(
                amenityIcon,
                amenity.getId().toString(),
                amenity.getName()
        );

        String fullPath = folder + fileName;
        return storageService.storeFile(amenityIcon, fullPath);
    }

    public void deleteAmenity(Amenity amenity) {
        storageService.deleteFile(amenity.getIcon());
    }

    public String updateAmenityImage(MultipartFile amenityIcon, Amenity amenity) {
        if (amenity.getIcon() != null && !amenity.getIcon().isEmpty()) {
            storageService.deleteFile(amenity.getIcon());
        }

        String newIconPath = storeAmenityImage(amenityIcon, amenity);

        return newIconPath;
    }

    private String generateSafeFileName(MultipartFile file, String id, String name) {
        String extension = StringUtils.getFilenameExtension(
                Objects.requireNonNullElse(file.getOriginalFilename(), "")
        );
        String safeName = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s_]+", "-");
        return id + "_" + safeName + "." + extension;
    }
}
