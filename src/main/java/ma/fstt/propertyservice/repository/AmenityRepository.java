package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    @Override
    boolean existsById(Long aLong);
}
