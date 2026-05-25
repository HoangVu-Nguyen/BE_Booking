package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.dto.event.NotificationCreatedEvent;
import clyvasync.Clyvasync.dto.response.NotificationResponse;
import clyvasync.Clyvasync.mapper.notification.NotificationMapper;
import clyvasync.Clyvasync.modules.notification.entity.Notification;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final SocketEmitterService socketEmitterService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNotificationCreated(NotificationCreatedEvent event) {
        Notification notification = event.notification();

        NotificationResponse response = NotificationMapper.toResponse(notification);

        log.info("[Realtime] Bắn thông báo qua Socket tới user: {}", notification.getRecipientId());

        socketEmitterService.sendNotification(notification.getRecipientId(), response);
    }
}