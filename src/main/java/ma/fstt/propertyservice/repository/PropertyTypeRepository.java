package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.model.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyTypeRepository extends JpaRepository<PropertyType, Long> {
    Optional<PropertyType> findByTypeIgnoreCase(String type);
}
