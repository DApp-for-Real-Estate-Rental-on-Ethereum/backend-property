package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddNewPropertyImagesRequest {
    private String coverImageName;
    @NotBlank
    private String propertyId;
}
