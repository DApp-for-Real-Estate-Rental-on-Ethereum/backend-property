package ma.fstt.propertyservice.service;

import ma.fstt.propertyservice.service.interfaces.IStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Service
@Primary
public class S3StorageService implements IStorageService {

    @Value("${aws.s3.bucket:derent-uploads}")
    private String bucketName;

    @Value("${aws.s3.region:us-east-1}")
    private String region;

    @Value("${aws.s3.prefix:uploads/}")
    private String keyPrefix;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Override
    public String storeFile(MultipartFile file) {
        String fileName = System.currentTimeMillis() + "_" + sanitizeFileName(file.getOriginalFilename());
        return storeFile(file, fileName);
    }

    @Override
    public String storeFile(MultipartFile file, String fileName) {
        try {
            String sanitizedFileName = sanitizeFileName(fileName);
            String key = keyPrefix + sanitizedFileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Return the full S3 URL
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + fileName, e);
        }
    }

    @Override
    public void deleteFile(String url) {
        try {
            // Extract key from URL
            String key = extractKeyFromUrl(url);
            if (key != null) {
                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();
                s3Client.deleteObject(deleteRequest);
            }
        } catch (Exception e) {
            // Log but don't throw - file may already be deleted
            System.err.println("Failed to delete file from S3: " + url + " - " + e.getMessage());
        }
    }

    private String extractKeyFromUrl(String url) {
        if (url == null) return null;
        
        // Handle S3 URL format: https://bucket.s3.region.amazonaws.com/key
        if (url.contains("amazonaws.com/")) {
            return url.substring(url.indexOf("amazonaws.com/") + "amazonaws.com/".length());
        }
        
        // Handle /uploads/ format (legacy)
        if (url.startsWith("/uploads/")) {
            return keyPrefix + url.substring("/uploads/".length());
        }
        
        return null;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unknown";
        // Remove potentially dangerous characters
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
