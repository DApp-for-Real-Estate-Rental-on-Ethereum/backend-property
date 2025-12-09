package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.model.AmenityCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityCategoryRepository extends JpaRepository<AmenityCategory, Long> {
}
