package clyvasync.Clyvasync.producer;

import clyvasync.Clyvasync.constant.MessagingConstants;
import clyvasync.Clyvasync.dto.event.UserEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendRegisterEvent(UserEventDTO payload) {
        rabbitTemplate.convertAndSend(
                MessagingConstants.REGISTER_EXCHANGE,
                MessagingConstants.REGISTER_ROUTING_KEY,
                payload
        );
    }
}
