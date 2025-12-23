package ma.fstt.propertyservice.dto.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricePredictionRequest {
    private String propertyId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}


