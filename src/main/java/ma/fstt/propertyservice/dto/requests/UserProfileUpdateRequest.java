package ma.fstt.propertyservice.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileUpdateRequest {
    private String userId;
    private Boolean complete;
}
