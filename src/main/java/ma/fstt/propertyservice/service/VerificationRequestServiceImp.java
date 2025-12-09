package ma.fstt.propertyservice.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.CreateVerificationRequestDto;
import ma.fstt.propertyservice.dto.requests.RejectReasonDto;
import ma.fstt.propertyservice.enums.PropertyStatusEnum;
import ma.fstt.propertyservice.enums.VerificationRequestStatusEnum;
import ma.fstt.propertyservice.exception.*;
import ma.fstt.propertyservice.model.Property;
import ma.fstt.propertyservice.model.VerificationRequest;
import ma.fstt.propertyservice.repository.VerificationRequestRepository;
import ma.fstt.propertyservice.service.interfaces.VerificationRequestService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class VerificationRequestServiceImp implements VerificationRequestService {
    private final PropertyService propertyService;
    private final VerificationRequestRepository verificationRequestRepository;

    @Override
    public void createVerificationRequest(CreateVerificationRequestDto input) {
        Property property = propertyService.getPropertyById(input.getPropertyId());
        if(!property.getUserId().equals(input.getUserId())) {
            throw new UserNotPermitedException();
        }
        if (verificationRequestRepository.existsByPropertyAndStatus(property, VerificationRequestStatusEnum.PENDING))
        {
            throw new VerificationRequestAlreadyPendingException();
        }
        VerificationRequest verificationRequest = new VerificationRequest();
        if (input.getDescription() != null) {
            verificationRequest.setDescription(input.getDescription());
        }
        verificationRequest.setCreatedAt(LocalDateTime.now());
        verificationRequest.setProperty(property);
        verificationRequest.setStatus(VerificationRequestStatusEnum.PENDING);
        verificationRequest.setDescription(input.getDescription());
        verificationRequestRepository.save(verificationRequest);
    }

    @Override
    public void deleteVerificationRequest(Long id, String userId) {
        VerificationRequest verificationRequest = getVerificationRequest(id);
        if(!verificationRequest.getProperty().getUserId().equals(userId)){
            throw new UserNotPermitedException();
        }
        if(verificationRequest.getStatus().equals(VerificationRequestStatusEnum.APPROVED) || verificationRequest.getStatus().equals(VerificationRequestStatusEnum.REJECTED)) {
            throw new VerificationRequestCannotBeDeleteException();
        }
        verificationRequestRepository.delete(verificationRequest);
    }

    @Override
    public void rejectVerificationRequest(Long id, RejectReasonDto input) {
        VerificationRequest verificationRequest = getVerificationRequest(id);
        if (!verificationRequest.getStatus().equals(VerificationRequestStatusEnum.PENDING)) {
            throw new RequestAlreadyProcessedException();
        }
        verificationRequest.setStatus(VerificationRequestStatusEnum.REJECTED);
        verificationRequest.setResponse(input.getReason());
        verificationRequestRepository.save(verificationRequest);
        propertyService.updatePropertyStatus(verificationRequest.getProperty(), PropertyStatusEnum.DISAPPROVED);
    }

    @Override
    public void approveVerificationRequest(Long id) {
        VerificationRequest verificationRequest = getVerificationRequest(id);
        if (!verificationRequest.getStatus().equals(VerificationRequestStatusEnum.PENDING)) {
            throw new RequestAlreadyProcessedException();
        }
        verificationRequest.setStatus(VerificationRequestStatusEnum.APPROVED);
        verificationRequest.setResponse(null);
        verificationRequestRepository.save(verificationRequest);
        propertyService.updatePropertyStatus(verificationRequest.getProperty(), PropertyStatusEnum.APPROVED);
    }

    @Override
    public List<VerificationRequest> getAllVerificationRequests() {
        return verificationRequestRepository.findAll();
    }

    @Override
    public VerificationRequest getVerificationRequestById(Long id) {
        return verificationRequestRepository.findById(id)
                .orElseThrow(VerificationRequestNotFoundException::new);
    }

    @Override
    public List<VerificationRequest> getVerificationRequestsByProperty(String propertyId) {
        Property property = propertyService.getPropertyById(propertyId);
        return verificationRequestRepository.findByProperty(property);
    }

    @Override
    public List<VerificationRequest> getVerificationRequestsByHost(String hostId) {
        return verificationRequestRepository.findByProperty_UserId(hostId);
    }

    @Override
    public List<VerificationRequest> getVerificationRequestsByStatus(String status) {
        VerificationRequestStatusEnum statusEnum;
        try {
            statusEnum = VerificationRequestStatusEnum.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        return verificationRequestRepository.findByStatus(statusEnum);
    }

    VerificationRequest getVerificationRequest(Long id) {
       return verificationRequestRepository.findById(id)
               .orElseThrow(VerificationRequestNotFoundException::new);
    }
}
