package ma.fstt.propertyservice.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.fstt.propertyservice.enums.VerificationRequestStatusEnum;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerificationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private VerificationRequestStatusEnum status;

    @Column
    private String description;

    @Column
    private String response;

    @ManyToOne
    @JoinColumn(name = "property_id")
    private Property property;
}
