package ma.fstt.propertyservice.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.UserProfileUpdateRequest;
import ma.fstt.propertyservice.exception.UserNotFoundException;
import ma.fstt.propertyservice.model.UserProfileStatus;
import ma.fstt.propertyservice.repository.UserProfileStatusRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileStatusRepository userProfileStatusRepository;

    public UserProfileStatus getUserProfileStatus(String hostId) {
        return userProfileStatusRepository.findById(hostId).orElse(null);
    }

    public Boolean userExists(String hostId) {
        return userProfileStatusRepository.existsById(hostId);
    }

    public Boolean isProfileComplete(String hostId) {
        UserProfileStatus userProfileStatus = getUserProfileStatus(hostId);
        if (userProfileStatus == null) {
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
        if (input.getUserId() == null) {
            return;
        }

        UserProfileStatus userProfileStatus = userProfileStatusRepository.findById(input.getUserId())
                .orElse(null);

        if (userProfileStatus == null) {
            userProfileStatus = new UserProfileStatus();
            userProfileStatus.setUserId(input.getUserId());
            userProfileStatus.setComplete(input.getComplete() != null ? input.getComplete() : false);
            userProfileStatusRepository.save(userProfileStatus);
        } else {
            if (input.getComplete() != null) {
                userProfileStatus.setComplete(input.getComplete());
            }
            userProfileStatusRepository.save(userProfileStatus);
        }
    }

    public void deleteUserProfileStatus(String hostId) {
        userProfileStatusRepository.deleteById(hostId);
    }
}
