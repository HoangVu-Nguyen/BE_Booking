package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.constant.SocketDestinations;

import clyvasync.Clyvasync.dto.event.BookingEvent;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final SocketEmitterService socketEmitterService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingRealtimeNotification(BookingEvent event) {
        log.info("[BOOKING-LISTENER] DB commit đặt phòng thành công. Tiến hành đẩy realtime.");

        // 1. Bắn tin nhắn riêng tư (Private) cho Host nhận đơn
        socketEmitterService.sendBookingNotification(event.getHostId(), event.getBookingPayload());

        // 2. Phát tín hiệu công khai (Broadcast) để đồng bộ trạng thái phòng/lịch phòng cho toàn hệ thống
        // Ví dụ payload chứa { roomId: 10, unavailableDates: ["2026-05-25", "2026-05-26"] }
        socketEmitterService.broadcastRoomStatus(SocketDestinations.ROOM_STATUS_TOPIC, event.getBookingPayload());
    }
}