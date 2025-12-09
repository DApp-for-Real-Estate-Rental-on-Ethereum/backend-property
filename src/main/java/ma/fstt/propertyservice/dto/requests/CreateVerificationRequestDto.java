package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateVerificationRequestDto {
    @NotNull
    private String propertyId;
    @NotNull
    private String userId;
    private String description;
}
