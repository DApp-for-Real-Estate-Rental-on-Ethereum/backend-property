package ma.fstt.propertyservice.config;

import ma.fstt.propertyservice.dto.requests.UserProfileUpdateRequest;
import ma.fstt.propertyservice.service.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQConsumer {

    private final UserService userService;

    public RabbitMQConsumer(UserService userService) {
        this.userService = userService;
    }

    @RabbitListener(queues = "${app.rabbitmq.user.queue}")
    public void receiveMessage(UserProfileUpdateRequest message) {
        userService.updateProfileStatus(message);
    }
}
