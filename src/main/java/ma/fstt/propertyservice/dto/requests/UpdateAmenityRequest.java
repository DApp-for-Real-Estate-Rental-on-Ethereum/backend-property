package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAmenityRequest {
    @NotBlank(message = "Amenity name is required")
    private String amenityName;
}
