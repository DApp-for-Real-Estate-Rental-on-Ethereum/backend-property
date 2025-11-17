package ma.fstt.propertyservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profile_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileStatus {
    @Id
    private String userId;

    @Column(name = "is_complete")
    private boolean isComplete;

    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean isDeleted;
}
