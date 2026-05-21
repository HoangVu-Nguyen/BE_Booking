package clyvasync.Clyvasync.listener;


import clyvasync.Clyvasync.dto.event.WalletEvent;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletEventListener {

    private final SocketEmitterService socketEmitterService;

    // CHÍ MẠNG Ở ĐÂY: phase = AFTER_COMMIT ép sự kiện chỉ chạy sau khi DB đã lưu xong 100%
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWalletNotificationAfterCommit(WalletEvent event) {
        log.info("[EVENT-LISTENER] DB đã commit thành công! Tiến hành đẩy Socket tới HostId: {}", event.getOwnerId());

        socketEmitterService.sendWalletNotification(event.getOwnerId(), event.getPayload());
    }
}
