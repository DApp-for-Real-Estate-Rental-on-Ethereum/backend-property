package ma.fstt.propertyservice.controller;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.CreateVerificationRequestDto;
import ma.fstt.propertyservice.dto.requests.RejectReasonDto;
import ma.fstt.propertyservice.service.interfaces.VerificationRequestService;
import ma.fstt.propertyservice.util.ParseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/verification-requests")
@RequiredArgsConstructor
public class VerificationRequestController {
    private final VerificationRequestService verificationRequestService;

    @PostMapping
    public ResponseEntity<?> createVerificationRequest(
            @RequestBody CreateVerificationRequestDto createVerificationRequestDto,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("HOST") && !roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        verificationRequestService.createVerificationRequest(createVerificationRequestDto);
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveVerificationRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
            ){
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        verificationRequestService.approveVerificationRequest(id);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectVerificationRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString,
            @RequestBody RejectReasonDto input
    ){
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        verificationRequestService.rejectVerificationRequest(id,input);
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelVerificationRequest(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Roles") String userRolesString
    ) {
        Set<String> roles = ParseUtil.StringToSet(userRolesString);
        if (!roles.contains("HOST") && !roles.contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        verificationRequestService.deleteVerificationRequest(id, userId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping
    public ResponseEntity<?> getAllVerificationRequests() {
        return ResponseEntity.ok(verificationRequestService.getAllVerificationRequests());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getVerificationRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(verificationRequestService.getVerificationRequestById(id));
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<?> getVerificationRequestsByProperty(@PathVariable String propertyId) {
        return ResponseEntity.ok(verificationRequestService.getVerificationRequestsByProperty(propertyId));
    }

    @GetMapping("/by-host/{hostId}")
    public ResponseEntity<?> getVerificationRequestsByHost(@PathVariable String hostId) {
        return ResponseEntity.ok(verificationRequestService.getVerificationRequestsByHost(hostId));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<?> getVerificationRequestsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(verificationRequestService.getVerificationRequestsByStatus(status));
    }

}
