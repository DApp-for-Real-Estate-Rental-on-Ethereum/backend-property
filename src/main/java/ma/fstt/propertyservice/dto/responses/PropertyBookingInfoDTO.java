package ma.fstt.propertyservice.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyBookingInfoDTO {
    private String id;
    private Long ownerId; // Convert from String userId to Long for booking-service compatibility
    private BigDecimal pricePerNight; // dailyPrice
    private Boolean isNegotiable; // Based on discountPlan or other logic
    private Boolean discountEnabled; // Based on discountPlan existence
    private Integer maxNegotiationPercent; // Can be calculated from discountPlan
    private Double negotiationPercentage; // Negotiation percentage (used as nicotine percentage for price calculation)
}

