package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ma.fstt.propertyservice.model.Amenity;

import java.util.Set;

@Data
public class UpdatePropertyRequest {
    private String title;
    private String description;
    private Double dailyPrice;
    @NotNull(message = "Number of guests is required.")
    @Min(value = 1, message = "Capacity must be at least 1 guest.")
    private Integer capacity;
    private Integer numberOfBedrooms;
    private Integer numberOfBeds;
    private Integer numberOfBathrooms;
    private Long typeId;
    private String address;
    private String city;
    private String country;
    private String zipCode; // Will be parsed to Integer in service
    @NotNull
    private Set<Amenity> amenities;
}
