package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.enums.VerificationRequestStatusEnum;
import ma.fstt.propertyservice.model.Property;
import ma.fstt.propertyservice.model.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    boolean existsByProperty(Property property);

    boolean existsByPropertyAndStatus(Property property, VerificationRequestStatusEnum status);

    List<VerificationRequest> findByStatus(VerificationRequestStatusEnum status);

    List<VerificationRequest> findByProperty(Property property);

    List<VerificationRequest> findByProperty_UserId(String hostId);
}
