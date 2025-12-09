package ma.fstt.propertyservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountPlan implements Serializable {
    @Column(name = "discount_five_days")
    private Integer fiveDays; // 10% discount

    @Column(name = "discount_fifteen_days")
    private Integer fifteenDays; // 15% discount

    @Column(name = "discount_one_month")
    private Integer oneMonth; // 20% discount
}

