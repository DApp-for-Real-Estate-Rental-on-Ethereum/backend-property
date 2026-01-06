package ma.fstt.propertyservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.*;
import ma.fstt.propertyservice.dto.responses.PricingPredictionResponse;
import ma.fstt.propertyservice.service.PricingService;
import ma.fstt.propertyservice.service.PropertyService;
import ma.fstt.propertyservice.service.UserProfileService;
import ma.fstt.propertyservice.util.ParseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import ma.fstt.propertyservice.model.Property;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {
    private static final Logger log = LoggerFactory.getLogger(PropertyController.class);
    private final PropertyService propertyService;
    private final UserProfileService userProfileService;
    private final PricingService pricingService;

    private boolean isAdmin(Set<String> roles) {
        return roles.contains("ADMIN");
    }

    private boolean isHost(Set<String> roles) {
        return roles.contains("HOST");
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createProperty(
            @RequestPart("input") @Valid CreatePropertyRequest input,
            @RequestPart("images") List<MultipartFile> images,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        log.info("createProperty request userId={} rolesRaw={}", userId, userRolesString);
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        log.info("createProperty parsedRoles={}", roles);
        if (!isHost(roles) && !isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "HOST role is required to create a property."));
        }
        boolean userExists = userProfileService.userExists(userId);

        if (!userExists) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error",
                            "User profile not verified. Please complete your profile and connect your wallet first."));
        }

        String propertyId = propertyService.createProperty(input, userId, images);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("propertyId", propertyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateProperty(
            @PathVariable String id,
            @RequestBody UpdatePropertyRequest input,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!isHost(roles) && !isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            propertyService.updateProperty(id, input, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body("Property updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("HOST") && !userProfileService.userExists(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.deleteProperty(id, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveProperty(
            @PathVariable String id,
            @RequestBody ApprovePropertyRequest input,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.approveProperty(id, input);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/hide")
    public ResponseEntity<?> hideProperty(
            @PathVariable String id,
            @RequestBody HidePropertyRequest input,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!isHost(roles) && !isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.hideProperty(id, userId, input);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<?> suspendProperty(
            @PathVariable String id,
            @RequestBody SuspensionPropertyRequest input,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.suspendProperty(id, input);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/revoke-suspension")
    public ResponseEntity<?> revokeSuspension(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.revokeSuspension(id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/submit-for-approval")
    public ResponseEntity<?> submitForApproval(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!isHost(roles) && !isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.submitForApproval(id, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PatchMapping("/{id}/cancel-approval-request")
    public ResponseEntity<?> cancelApprovalRequest(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!isHost(roles) && !isAdmin(roles)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        propertyService.cancelApprovalRequest(id, userId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping
    public ResponseEntity<List<Property>> getAllProperties(
            @RequestHeader(value = "X-User-Roles", required = false) String userRolesString) {
        if (userRolesString != null) {
            Set<String> roles = ParseUtil.StringToSet(userRolesString);
            if (roles.contains("ADMIN")) {
                List<Property> properties = propertyService.getAllProperties();
                return ResponseEntity.ok(properties);
            }
        }
        List<Property> properties = propertyService.getAllApprovedProperties();
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<List<Property>> getAllPropertiesForAdmin(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        try {
            Set<String> roles = ParseUtil.StringToSet(userRolesString);

            if (!roles.contains("ADMIN")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Collections.emptyList());
            }

            List<Property> properties = propertyService.getAllProperties();
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPropertyById(@PathVariable String id) {
        try {
            Property property = propertyService.getPropertyById(id);
            return ResponseEntity.ok(property);
        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch property: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/booking-info")
    public ResponseEntity<ma.fstt.propertyservice.dto.responses.PropertyBookingInfoDTO> getPropertyBookingInfo(
            @PathVariable String id) {
        ma.fstt.propertyservice.dto.responses.PropertyBookingInfoDTO info = propertyService.getPropertyBookingInfo(id);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/my-properties")
    public ResponseEntity<List<Property>> getMyProperties(
            @RequestHeader("X-User-Id") String userId) {
        try {
            List<Property> properties = propertyService.getPropertiesByUserId(userId);
            return ResponseEntity.ok(properties);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/{id}/predict-price")
    public ResponseEntity<?> predictPrice(
            @PathVariable String id,
            @RequestBody PricePredictionRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString) {
        try {
            // Verify property exists
            Property property = propertyService.getPropertyById(id);
            if (property == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Property not found"));
            }

            // Check authorization (owner or admin)
            Set<String> roles = ParseUtil.StringToSet(userRolesString);
            if (!property.getUserId().equals(userId) && !isAdmin(roles)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You don't have permission to predict price for this property"));
            }

            // Validate dates
            if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "checkInDate and checkOutDate are required"));
            }

            if (request.getCheckOutDate().isBefore(request.getCheckInDate()) ||
                    request.getCheckOutDate().isEqual(request.getCheckInDate())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "checkOutDate must be after checkInDate"));
            }

            // Get price prediction
            PricingPredictionResponse prediction = pricingService.predictPrice(
                    id,
                    request.getCheckInDate(),
                    request.getCheckOutDate());

            return ResponseEntity.ok(prediction);

        } catch (RuntimeException e) {
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to predict price: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
}
