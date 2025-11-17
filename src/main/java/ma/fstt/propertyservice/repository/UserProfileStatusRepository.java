package ma.fstt.propertyservice.repository;

import ma.fstt.propertyservice.model.UserProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileStatusRepository extends JpaRepository<UserProfileStatus, String> {
}
