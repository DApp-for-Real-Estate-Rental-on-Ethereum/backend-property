package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.model.Property;
import ma.fstt.propertyservice.model.Suspension;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
    Optional<Suspension> findByPropertyAndActiveTrue(Property property);
}
