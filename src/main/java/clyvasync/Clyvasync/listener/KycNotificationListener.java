package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.dto.event.KycProcessedEvent;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KycNotificationListener {

    private final SocketEmitterService socketEmitterService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleKycProcessedEvent(KycProcessedEvent event) {
        log.info(">>>> [Event Listener] Gửi thông báo Socket cho User: {}", event.getUserId());

        Map<String, Object> payload = Map.of(
                "status", event.getStatus(),
                "message", event.getStatus().name().equals("APPROVED")
                        ? "Hồ sơ của bạn đã được duyệt!"
                        : "Hồ sơ bị từ chối: " + event.getReason()
        );

        socketEmitterService.sendNotification(event.getUserId(), payload);
    }
}