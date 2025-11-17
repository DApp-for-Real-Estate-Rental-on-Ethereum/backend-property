package ma.fstt.propertyservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Table(name = "property_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyImage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String url;

    @Column(name = "is_cover", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean cover;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "propety_id")
    private Property property;
}
