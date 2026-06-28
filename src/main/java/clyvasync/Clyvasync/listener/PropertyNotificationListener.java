package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.dto.event.PropertyVerificationEvent;
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
public class PropertyNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePropertyVerificationEvent(PropertyVerificationEvent event) {
        log.info(">>>> [Event Listener] Gửi thông báo Duyệt Tài sản cho User: {}", event.getUserId());

        boolean isApproved = HomestayStatus.APPROVED.equals(event.getStatus());

        String title = isApproved
                ? "Tài sản của bạn đã được duyệt"
                : "Tài sản của bạn cần cập nhật lại giấy tờ";

        String message = isApproved
                ? "Tuyệt vời! Chỗ nghỉ '" + event.getHomestayName() + "' đã sẵn sàng đón khách."
                : "Hồ sơ của '" + event.getHomestayName() + "' bị từ chối do có tài liệu không hợp lệ. Vui lòng kiểm tra lại.";

        Map<String, Object> metadata = Map.of(
                "homestayId", event.getHomestayId(),
                "status", event.getStatus(),
                "link", isApproved ? "/host/properties" : "/host/properties/edit/" + event.getHomestayId()
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
            log.error("Lỗi khi gửi thông báo Property Verification: {}", e.getMessage());
        }
    }
}