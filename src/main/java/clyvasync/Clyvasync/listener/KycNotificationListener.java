package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.dto.event.KycProcessedEvent;
import clyvasync.Clyvasync.enums.type.NotificationType;
import clyvasync.Clyvasync.service.notification.NotificationService;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KycNotificationListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleKycProcessedEvent(KycProcessedEvent event) {
        log.info(">>>> [Event Listener] Gửi thông báo KYC cho User: {}", event.getUserId());

        // 1. Xác định nội dung
        boolean isApproved = event.getStatus().name().equals("APPROVED");
        String title = isApproved ? "Hồ sơ xác thực đã được duyệt" : "Hồ sơ xác thực bị từ chối";
        String message = isApproved
                ? "Chúc mừng! Tài khoản của bạn đã sẵn sàng sử dụng."
                : "Lý do: " + event.getReason();
        Map<String, Object> metadata = Map.of(
                "status", event.getStatus().name(),
                "link", isApproved ? "/dashboard" : "/register-host/upload"
        );


        try{
            notificationService.sendNotification(
                    event.getUserId(),
                    NotificationType.SYSTEM,
                    title,
                    message,
                    metadata
            );
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

}