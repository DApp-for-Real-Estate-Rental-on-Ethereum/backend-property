package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ApprovePropertyRequest {
    @NotNull
    Boolean isApproved;
}
