package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PropertyRepository extends JpaRepository<Property, Long> {
}
