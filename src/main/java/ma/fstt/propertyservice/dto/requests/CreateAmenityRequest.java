package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAmenityRequest {
    @NotBlank(message = "Amenity name is required")
    private String amenityName;

    private Long categoryId;

    private String amenityCategoryTitle;
}
