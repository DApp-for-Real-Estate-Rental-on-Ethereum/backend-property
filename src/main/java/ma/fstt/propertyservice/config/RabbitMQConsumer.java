package ma.fstt.propertyservice.config;

import ma.fstt.propertyservice.dto.requests.UserProfileUpdateRequest;
import ma.fstt.propertyservice.service.UserProfileService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    private final UserProfileService userProfileService;

    public RabbitMQConsumer(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @RabbitListener(queues = "${app.rabbitmq.user.queue}")
    public void receiveMessage(UserProfileUpdateRequest message) {
        userProfileService.updateProfileStatus(message);
    }
}
