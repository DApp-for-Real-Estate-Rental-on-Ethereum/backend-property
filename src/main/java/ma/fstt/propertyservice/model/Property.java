package ma.fstt.propertyservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;
import lombok.*;
import ma.fstt.propertyservice.enums.PropertyStatusEnum;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "properties")
@DynamicUpdate
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Property implements Serializable {
    @Id
    private String id;

    @Column(name = "user_id")
    private String userId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PropertyStatusEnum status;

    @Column(name = "daily_price", nullable = false)
    private Double dailyPrice;

    @Column(name = "deposit_amount", nullable = false, columnDefinition = "DOUBLE PRECISION DEFAULT 0.0")
    private Double depositAmount;

    @Column(name = "price", nullable = false)
    private Double price; // Legacy column - kept for database compatibility, always set to same value as dailyPrice

    @Column(name = "negotiation_percentage", nullable = false)
    private Double negotiationPercentage;

    @Embedded
    private DiscountPlan discountPlan;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "number_of_bedrooms", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer numberOfBedrooms;

    @Column(name = "number_of_beds", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer numberOfBeds;

    @Column(name = "number_of_bathrooms", nullable = false, columnDefinition = "INTEGER DEFAULT 1")
    private Integer numberOfBathrooms;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Address address;

    @ManyToOne
    @JoinColumn(name = "type_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PropertyType type;

    @OneToMany(mappedBy = "property")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<PropertyImage> propertyImages;

    @OneToMany(mappedBy = "property", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    private Set<Availability> availabilities;

    @ManyToMany
    @JoinTable(
            name = "properties_amenities",
            joinColumns = @JoinColumn(name = "property_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Set<Amenity> amenities;

    @OneToMany(mappedBy = "property")
    @JsonIgnore
    private Set<Suspension> suspensions;

    @OneToMany(mappedBy = "property")
    @JsonIgnore
    private Set<VerificationRequest> verificationRequests;
}

