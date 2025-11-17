package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.propertyservice.enums.PropertyTypeEnum;
import ma.fstt.propertyservice.model.Amenity;

import java.util.Set;

@Getter
@Setter
public class CreatePropertyRequest {

    @NotBlank(message = "Title is required and must not be blank.")
    private String title;

    @NotBlank(message = "Description is required and must not be blank.")
    private String description;

    @NotNull(message = "Property type is required and cannot be null.")
    private PropertyTypeEnum type;

    @NotNull(message = "Price is required.")
    private Double price;

    @NotNull(message = "Number of guests is required.")
    @Min(value = 1, message = "Capacity must be at least 1 guest.")
    private Integer capacity;

    @NotNull(message = "Number of bedrooms is required.")
    private Integer numberOfBedrooms;

    @NotNull(message = "Number of bathrooms is required.")
    @Min(value = 1, message = "Bathrooms must be at least 1.")
    private Integer numberOfBathrooms;

    @NotNull(message = "Number of beds is required.")
    @Min(value = 1, message = "Beds must be at least 1.")
    private Integer numberOfBeds;

    @NotNull
    private Set<Amenity> amenities;

    @NotNull(message = "The 'draft' status must be set (true or false).")
    private Boolean draft;

    @NotNull(message = "Address is required")
    @NotBlank
    private String address;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Country is required")
    private String country;

    @NotNull(message = "City is required")
    private String city;

    @NotNull(message = "Postal code is required")
    private Integer postalCode;

    @NotNull
    private String coverImageName;
}