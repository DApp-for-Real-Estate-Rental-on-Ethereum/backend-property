package ma.fstt.propertyservice.service.interfaces;

import ma.fstt.propertyservice.dto.requests.CreateVerificationRequestDto;
import ma.fstt.propertyservice.dto.requests.RejectReasonDto;
import ma.fstt.propertyservice.model.VerificationRequest;

import java.util.List;

public interface VerificationRequestService {
    void createVerificationRequest(CreateVerificationRequestDto input);
    void deleteVerificationRequest(Long id, String userId);
    void rejectVerificationRequest(Long id, RejectReasonDto input);
    void approveVerificationRequest(Long id);
    List<VerificationRequest> getAllVerificationRequests();
    VerificationRequest getVerificationRequestById(Long id);
    List<VerificationRequest> getVerificationRequestsByProperty(String propertyId);
    List<VerificationRequest> getVerificationRequestsByHost(String hostId);
    List<VerificationRequest> getVerificationRequestsByStatus(String status);
}
