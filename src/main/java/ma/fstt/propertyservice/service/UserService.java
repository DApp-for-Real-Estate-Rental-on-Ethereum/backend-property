package ma.fstt.propertyservice.service;

import ma.fstt.propertyservice.dto.requests.UserProfileUpdateRequest;
import ma.fstt.propertyservice.model.UserProfileStatus;
import ma.fstt.propertyservice.repository.UserProfileStatusRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserProfileStatusRepository userProfileStatusRepository;

    public UserService(UserProfileStatusRepository userProfileStatusRepository) {
        this.userProfileStatusRepository = userProfileStatusRepository;
    }

    public UserProfileStatus getUserProfileStatus(String hostId) {
        return userProfileStatusRepository.findById(hostId).orElse(null);
    }

    public Boolean userExists(String hostId) {
        return userProfileStatusRepository.existsById(hostId);
    }

    public Boolean isProfileComplete(String hostId) {
        UserProfileStatus userProfileStatus = getUserProfileStatus(hostId);
        if (userProfileStatus == null) {
            createUserProfileStatus(hostId, false);
            return false;
        }else
        {
            return userProfileStatus.isComplete();
        }
    }

    private void createUserProfileStatus(String hostId, Boolean complete) {
        UserProfileStatus userProfileStatus = new UserProfileStatus();
            userProfileStatus.setComplete(complete);
            userProfileStatus.setUserId(hostId);
        userProfileStatusRepository.save(userProfileStatus);
    }

    public void updateProfileStatus(UserProfileUpdateRequest input) {
        if (input.getUserId() == null || input.getComplete() == null) {
            System.out.println("Error");
           return;
        }

        UserProfileStatus userProfileStatus = userProfileStatusRepository.findById(input.getUserId())
                .orElse(new UserProfileStatus());

        userProfileStatus.setUserId(input.getUserId());
        userProfileStatus.setComplete(input.getComplete());

        userProfileStatusRepository.save(userProfileStatus);
    }

    public void deleteUserProfileStatus(String hostId) {
        userProfileStatusRepository.deleteById(hostId);
    }
}
