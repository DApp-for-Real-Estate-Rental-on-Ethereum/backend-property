package ma.fstt.propertyservice.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricingPredictionResponse {
    private Double predictedPriceMad;
    private Double predictedPriceUsd;
    private Double confidenceIntervalLower;
    private Double confidenceIntervalUpper;
    private String city;
    private String season;
    private String modelVersion;
    private String predictionTimestamp;
    private Double currentPriceMad;
    private Double priceDifferencePercent;
    private String recommendation; // INCREASE, DECREASE, MAINTAIN
}


