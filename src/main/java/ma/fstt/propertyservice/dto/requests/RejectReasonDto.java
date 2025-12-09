package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectReasonDto {
    @NotBlank
    private String reason;
}
