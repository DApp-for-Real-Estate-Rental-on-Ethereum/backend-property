package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class HidePropertyRequest {
    @NotNull
    Boolean isHidden;
}
