package ma.fstt.propertyservice.controller;

import ma.fstt.propertyservice.dto.requests.CreateAmenityRequest;
import ma.fstt.propertyservice.service.AmenityService;
import ma.fstt.propertyservice.util.ParseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/amenities")
public class AmenityController {
    private final AmenityService amenityService;

    public AmenityController(AmenityService amenityService) {
        this.amenityService = amenityService;
    }

    @PostMapping
    public ResponseEntity<?> createAmenity(
            @RequestPart CreateAmenityRequest input,
            @RequestPart MultipartFile amenityIcon,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        if(!ParseUtil.StringToSet(userRolesString).contains("ADMIN"))
        {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        else
        {
            amenityService.createAmenity(input,amenityIcon);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
    }

}
