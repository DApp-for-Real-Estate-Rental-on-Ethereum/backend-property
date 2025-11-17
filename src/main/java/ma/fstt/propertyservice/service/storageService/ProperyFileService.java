package ma.fstt.propertyservice.service.storageService;

import ma.fstt.propertyservice.model.Property;
import ma.fstt.propertyservice.model.PropertyImage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ProperyFileService extends StorageService {

    /**
     * Use a static final constant for the folder path
     */
    private static final String UPLOAD_FOLDER = "/uploads/properties/";

    /**
    * Stores property images
    * @return a Set of PropertyImage
    */
    public Set<PropertyImage> storePropertyImages(List<MultipartFile> files, Property property, String coverImageName) {
        if (files == null || files.isEmpty()) {
            return new HashSet<>();
        }

        Set<PropertyImage> images = new HashSet<>();
        Set<String> uploadedUrls = new HashSet<>();

        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);

                if (file == null || file.isEmpty()) {
                    continue;
                }

                String extension = StringUtils.getFilenameExtension(
                        Objects.requireNonNullElse(file.getOriginalFilename(), "")
                );

                String fileName = property.getId() + "_" + i + "." + extension;
                String fullPath = UPLOAD_FOLDER + fileName;

                String url = storeFile(file, fullPath);

                uploadedUrls.add(url);

                PropertyImage propertyImage = new PropertyImage();
                propertyImage.setUrl(url);
                propertyImage.setProperty(property);
                propertyImage.setCover(coverImageName.equals(file.getOriginalFilename()));
                images.add(propertyImage);
            }
        } catch (Exception e) {

            for (String url : uploadedUrls) {
                try {
                    deleteFile(url);
                } catch (Exception deleteException) {
                    System.err.println("Failed to delete file during rollback: " + url + " " + deleteException.getMessage());
                }
            }
            throw new RuntimeException("Failed to upload images. Rolling back.", e);
        }

        return images;
    }
}