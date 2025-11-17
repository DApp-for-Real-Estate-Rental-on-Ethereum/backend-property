package ma.fstt.propertyservice.controller;

import jakarta.validation.Valid;
import ma.fstt.propertyservice.dto.requests.CreatePropertyRequest;
import ma.fstt.propertyservice.dto.requests.UpdatePropertyRequest;
import ma.fstt.propertyservice.service.PropertyService;
import ma.fstt.propertyservice.service.UserService;
import ma.fstt.propertyservice.util.ParseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {
    private final PropertyService propertyService;
    private final UserService userService;

    public PropertyController(PropertyService propertyService, UserService userService) {
        this.propertyService = propertyService;
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProperty(
            @RequestPart @Valid CreatePropertyRequest input,
            @RequestPart("images") List<MultipartFile> images,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);

        if ((!roles.contains("TENANT")
                || !roles.contains("ADMIN")
                || !roles.contains("HOST"))
                && !userService.userExists(userId)
        ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        propertyService.createProperty(input, userId, images);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProperty(
            @PathVariable String id,
            @RequestBody UpdatePropertyRequest input
    )
    {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
