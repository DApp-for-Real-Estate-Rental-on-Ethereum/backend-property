package ma.fstt.propertyservice.service.storageService;

import ma.fstt.propertyservice.model.Amenity;
import ma.fstt.propertyservice.model.AmenityCategory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.Objects;

@Service
public class AmenityStorageService extends StorageService {

    // Define the paths as constants
    private static final String AMENITY_PATH = "/uploads/amenities/";

    /**
     * Stores an amenity icon.
     * @return The full path to the stored file, or null if no file was provided.
     */
    public String storeAmenity(MultipartFile amenityIcon, Amenity amenity) {
        // 1. Handle optional/null file
        if (amenityIcon == null || amenityIcon.isEmpty()) {
            return null;
        }

        // 2. Generate a safe filename
        String fileName = generateSafeFileName(
                amenityIcon,
                amenity.getId().toString(),
                amenity.getName()
        );

        // 3. Build the full path
        String fullPath = AMENITY_PATH + fileName;

        return super.storeFile(amenityIcon, fullPath);
    }

    /**
     * Generates a safe, URL-friendly filename.
     * Example: (file: "pool icon.svg", id: "1", name: "Pool Area!")
     * Becomes: "1_pool-area.svg"
     */
    private String generateSafeFileName(MultipartFile file, String id, String name) {
        // Get the file extension (e.g., "png", "svg")
        String extension = StringUtils.getFilenameExtension(
                Objects.requireNonNullElse(file.getOriginalFilename(), "")
        );

        // Sanitize the name (slugify)
        // "Pool Area!" -> "pool-area"
        String safeName = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove special chars
                .replaceAll("[\\s_]+", "-");      // Replace spaces/underscores with a dash

        return id + "_" + safeName + "." + extension;
    }
}