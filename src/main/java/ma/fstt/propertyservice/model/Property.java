package ma.fstt.propertyservice.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

import lombok.*;
import ma.fstt.propertyservice.enums.PropertyTypeEnum;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PropertyTypeEnum type;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    // Address
    @Column
    private Double longitude;
    @Column
    private Double latitude;
    @Column
    private String address;
    @Column
    private String city;
    @Column
    private String country;
    @Column
    private Integer postalCode;

    @Builder.Default
    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private Boolean deleted = false;

    @Builder.Default
    @Column(name = "is_draft", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean draft = false;

    @OneToMany(mappedBy = "property")
    private Set<PropertyImage> propertyImages;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Rule> rules;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Availability> availabilities;

    @ManyToMany
    @JoinTable(
            name = "properties_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities;

}

