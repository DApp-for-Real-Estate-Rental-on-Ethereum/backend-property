package ma.fstt.propertyservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.AddNewPropertyImagesRequest;
import ma.fstt.propertyservice.service.PropertyService;
import ma.fstt.propertyservice.util.ParseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/property-images")
@RequiredArgsConstructor
public class PropertyImagesController {
    private final PropertyService propertyService;

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePropertyImage(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
    ){
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("HOST") && !roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.deletePropertyImage(id, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePropertyImage(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
    )
    {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("HOST") && !roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.updatePropertyImage(id,userId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Property images updated");
    }

    @PostMapping
    public ResponseEntity<?> addNewPropertyImages(
            @Valid @RequestPart("input") AddNewPropertyImagesRequest input,
            @RequestPart("images") List<MultipartFile> newImages,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
    )
    {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("HOST") && !roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.addNewPropertyImages(input,userId, newImages);
        return ResponseEntity.status(HttpStatus.CREATED).body("Property images updated");
    }

}
