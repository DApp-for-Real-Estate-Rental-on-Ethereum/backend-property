package ma.fstt.propertyservice.controller;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.CategoryUpdateRequest;
import ma.fstt.propertyservice.dto.requests.CreateAmenityRequest;
import ma.fstt.propertyservice.dto.requests.UpdateAmenityRequest;
import ma.fstt.propertyservice.service.AmenityService;
import ma.fstt.propertyservice.util.ParseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/amenities")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    private boolean isAdmin(String userRolesString) {
        return ParseUtil.StringToSet(userRolesString).contains("ADMIN");
    }

    @PostMapping
    public ResponseEntity<?> createAmenity(
            @RequestPart CreateAmenityRequest input,
            @RequestPart MultipartFile amenityIcon,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        if (!isAdmin(userRolesString)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        amenityService.createAmenity(input, amenityIcon);
        return ResponseEntity.status(HttpStatus.OK).body("Amenity created successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAmenity(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        if (!isAdmin(userRolesString)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        amenityService.deleteAmenity(id);
        return ResponseEntity.status(HttpStatus.OK).body("Amenity has been deleted");
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<?> deleteAmenityCategory(
            @PathVariable Long id,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        if (!isAdmin(userRolesString)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        amenityService.deleteAmenityCategory(id);
        return ResponseEntity.status(HttpStatus.OK).body("Category has been deleted");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAmenity(
            @PathVariable Long id,
            @RequestPart UpdateAmenityRequest input,
            @RequestPart(required = false) MultipartFile amenityIcon,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        if (!isAdmin(userRolesString)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        amenityService.updateAmenity(id, input, amenityIcon);
        return ResponseEntity.status(HttpStatus.OK).body("Amenity updated successfully");
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<String> updateAmenityCategory(
            @PathVariable Long id,
            @RequestBody CategoryUpdateRequest input,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        if (!isAdmin(userRolesString)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        amenityService.updateAmenityCategory(id, input);
        return ResponseEntity.status(HttpStatus.OK).body("Category updated successfully");
    }
}
