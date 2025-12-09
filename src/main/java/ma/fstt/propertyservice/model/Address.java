package ma.fstt.propertyservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "addresses")
@Data
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    @Column(name = "postal_code")
    private Integer zipCode;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Property property;
}
