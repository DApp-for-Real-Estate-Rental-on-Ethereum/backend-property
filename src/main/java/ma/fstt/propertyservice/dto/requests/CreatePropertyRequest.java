package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import ma.fstt.propertyservice.model.Address;
import ma.fstt.propertyservice.model.Amenity;
import ma.fstt.propertyservice.model.DiscountPlan;
import ma.fstt.propertyservice.model.Property;

import java.util.Set;

@Getter
@Setter
public class CreatePropertyRequest {

    @NotBlank(message = "Title is required and must not be blank.")
    private String title;

    @NotBlank(message = "Description is required and must not be blank.")
    private String description;

    @NotNull(message = "Property type is required and cannot be null.")
    private Long typeId;

    @NotNull(message = "Daily price is required.")
    private Double dailyPrice;

    @NotNull(message = "Deposit amount is required.")
    @Min(value = 0, message = "Deposit amount must be non-negative.")
    private Double depositAmount;

    @NotNull(message = "negotiation percentage is required.")
    private Double negotiationPercentage;

    private DiscountPlanDTO discountPlan;

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

    @NotNull(message = "Address is required")
    private Address address;

    private String coverImageName;

    public Property createProperty() {
        DiscountPlan plan = null;
        if (discountPlan != null) {
            plan = DiscountPlan.builder()
                    .fiveDays(discountPlan.getFiveDays())
                    .fifteenDays(discountPlan.getFifteenDays())
                    .oneMonth(discountPlan.getOneMonth())
                    .build();
        }
        
        Property property = Property.builder()
                .title(title)
                .description(description)
                .dailyPrice(dailyPrice)
                .depositAmount(depositAmount)
                .discountPlan(plan)
                .capacity(capacity)
                .numberOfBedrooms(numberOfBedrooms)
                .numberOfBeds(numberOfBeds)
                .numberOfBathrooms(numberOfBathrooms)
                .address(address)
                .build();
        
        property.setNegotiationPercentage(negotiationPercentage);
        
        return property;
    }

    @Getter
    @Setter
    public static class DiscountPlanDTO {
        private Integer fiveDays;
        private Integer fifteenDays;
        private Integer oneMonth;
    }
}
