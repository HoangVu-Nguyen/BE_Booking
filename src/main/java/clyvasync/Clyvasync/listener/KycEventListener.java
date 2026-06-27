package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.constant.MessagingConstants;
import clyvasync.Clyvasync.dto.event.KycSubmittedEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
@Slf4j
@AllArgsConstructor
@Component
public class KycEventListener {
    private final RabbitTemplate rabbitTemplate;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleKycSubmittedEvent(KycSubmittedEvent event) {
        rabbitTemplate.convertAndSend(
                MessagingConstants.KYC_EXCHANGE,
                MessagingConstants.KYC_SUBMITTED_ROUTING_KEY,
                event
        );
        log.info(">>>> [MQ] Đã bắn event yêu cầu eKYC cho Profile ID: {}", event.getProfileId());
    }
}
