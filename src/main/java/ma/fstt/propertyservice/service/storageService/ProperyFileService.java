package ma.fstt.propertyservice.service.storageService;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.model.Property;
import ma.fstt.propertyservice.model.PropertyImage;
import ma.fstt.propertyservice.service.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProperyFileService {

    private final IStorageService storageService;

    @Value("${storage.local.directory:uploads}")
    private String folder;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    public Set<PropertyImage> storePropertyImages(List<MultipartFile> files,
                                                  Property property,
                                                  String coverImageName) {

        if (files == null || files.isEmpty()) {
            return new HashSet<>();
        }

        Set<PropertyImage> images = new HashSet<>();
        Set<String> uploadedPaths = new HashSet<>();

        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);

                if (file == null || file.isEmpty()) continue;

                String extension = StringUtils.getFilenameExtension(
                        Objects.requireNonNullElse(file.getOriginalFilename(), "")
                );
                if (extension == null) {
                    throw new IllegalArgumentException("File must have an extension");
                }

                extension = extension.toLowerCase();
                if (!ALLOWED_EXTENSIONS.contains(extension)) {
                    throw new IllegalArgumentException("Allowed formats: JPG, JPEG, PNG, WEBP");
                }

                String mimeType = file.getContentType();
                if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                    throw new IllegalArgumentException("Invalid MIME type: " + mimeType);
                }

                String fileName = property.getId() + "_" + i + "." + extension;

                String url = storageService.storeFile(file, fileName);
                uploadedPaths.add(url);

                PropertyImage propertyImage = new PropertyImage();
                propertyImage.setUrl(url);
                propertyImage.setProperty(property);

                boolean isCover =
                        (coverImageName == null && i == 0)
                                ||
                                (coverImageName != null
                                        && coverImageName.equals(file.getOriginalFilename()));

                propertyImage.setCover(isCover);

                images.add(propertyImage);
            }
        }
        catch (Exception e) {
            for (String fullPath : uploadedPaths) {
                try {
                    storageService.deleteFile(fullPath);
                } catch (Exception deleteError) {
                }
            }
            throw new RuntimeException("Failed to upload images. Rollback executed.", e);
        }

        return images;
    }

    public void deletePropertyImage(PropertyImage propertyImage) {
        storageService.deleteFile(propertyImage.getUrl());
    }
}
