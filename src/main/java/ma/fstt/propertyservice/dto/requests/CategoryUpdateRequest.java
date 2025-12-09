package ma.fstt.propertyservice.dto.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {
    @NotBlank(message = "Category name is required")
    private String categoryName;
}
