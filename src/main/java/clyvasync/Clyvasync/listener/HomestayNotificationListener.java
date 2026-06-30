package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.dto.event.HomestayStatusChangedEvent;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.type.NotificationType;
import clyvasync.Clyvasync.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomestayNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHomestayStatusChanged(HomestayStatusChangedEvent event) {
        log.info(">>>> [Event Listener] Gửi thông báo trạng thái tài sản cho Host: {}", event.getUserId());

        boolean isSuspended = event.getNewStatus().equals(HomestayStatus.SUSPENDED);
        String title = isSuspended ? "Căn hộ của bạn đã bị tạm dừng" : "Căn hộ của bạn đã được kích hoạt";
        String message = isSuspended
                ? "Căn hộ '" + event.getHomestayName() + "' đã bị khóa. Lý do: " + event.getReason()
                : "Căn hộ '" + event.getHomestayName() + "' đã được mở lại và hiện đã hiển thị công khai.";

        Map<String, Object> metadata = Map.of(
                "homestayId", event.getHomestayId(),
                "status", event.getNewStatus()
        );

        try {
            notificationService.sendNotification(
                    event.getUserId(),
                    NotificationType.SYSTEM,
                    title,
                    message,
                    metadata
            );
        } catch (Exception e) {
            log.error(">>>> [Event Listener] Lỗi gửi thông báo: {}", e.getMessage());
        }
    }
}