package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePropertyTypeRequest {

    @NotBlank(message = "Property type is required.")
    private String type;
}
