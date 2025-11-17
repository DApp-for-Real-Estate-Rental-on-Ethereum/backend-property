package ma.fstt.propertyservice.service.storageService;

import ma.fstt.propertyservice.service.interfaces.IStorageService;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class StorageService implements IStorageService {

    private final Path storagePath;

    public StorageService() {
        this.storagePath = Path.of("/Users/achraf/Desktop/dapp/test-images");
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        return storeFile(file, fileName);
    }

    @Override
    public String storeFile(MultipartFile file, String fileName) {
        try {
            Path target = this.storagePath.resolve(storagePath + fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store file " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String url) {
        try {
            Path filePath = this.storagePath.resolve(url).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete file: " + url, ex);
        }
    }

    public UrlResource loadFile(String fileName) {
        try {
            Path filePath = this.storagePath.resolve(fileName).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

}
