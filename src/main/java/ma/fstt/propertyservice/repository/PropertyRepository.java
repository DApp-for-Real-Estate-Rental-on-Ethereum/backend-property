package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.enums.PropertyStatusEnum;
import ma.fstt.propertyservice.model.Property;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {
    @Query("SELECT count(p) from Property p JOIN p.amenities a WHERE a.id = :amenityId")
    Long countByAmenityId(Long amenityId);

    @EntityGraph(attributePaths = {"propertyImages", "address", "type", "amenities"})
    @Query("SELECT p FROM Property p WHERE p.status = :status")
    List<Property> findAllByStatus(@Param("status") PropertyStatusEnum status);

    @EntityGraph(attributePaths = {"propertyImages", "address", "type", "amenities"})
    @Query("SELECT p FROM Property p WHERE p.id = :id")
    java.util.Optional<Property> findByIdWithDetails(@Param("id") String id);

    @EntityGraph(attributePaths = {"propertyImages", "address", "type", "amenities"})
    @Query("SELECT p FROM Property p WHERE p.userId = :userId")
    List<Property> findAllByUserId(@Param("userId") String userId);

    @EntityGraph(attributePaths = {"propertyImages", "address", "type", "amenities"})
    @Query("SELECT p FROM Property p")
    List<Property> findAllWithDetails();
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query(value = "UPDATE properties SET description = :description, daily_price = :dailyPrice, price = :price, capacity = :capacity, number_of_bedrooms = :bedrooms, number_of_beds = :beds, number_of_bathrooms = :bathrooms WHERE id = :id", nativeQuery = true)
    int updatePropertyFields(@Param("id") String id, 
                             @Param("description") String description,
                             @Param("dailyPrice") Double dailyPrice,
                             @Param("price") Double price,
                             @Param("capacity") Integer capacity,
                             @Param("bedrooms") Integer bedrooms,
                             @Param("beds") Integer beds,
                             @Param("bathrooms") Integer bathrooms);
}
