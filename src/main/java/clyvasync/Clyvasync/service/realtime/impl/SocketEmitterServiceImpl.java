package clyvasync.Clyvasync.service.realtime.impl;

import clyvasync.Clyvasync.constant.SocketDestinations; // <-- Import hằng số

import clyvasync.Clyvasync.dto.detail.WalletNotificationPayload;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketEmitterServiceImpl implements SocketEmitterService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendWalletNotification(Long ownerId, WalletNotificationPayload payload) {
        try {
            log.info("[SOCKET-EMITTER] Đang đẩy thông báo ví tới HostId: {}", ownerId);

            // Sử dụng hằng số tập trung thay vì viết chuỗi cứng
            messagingTemplate.convertAndSendToUser(
                    ownerId.toString(),
                    SocketDestinations.WALLET_QUEUE,
                    payload
            );
        } catch (Exception e) {
            log.error("[SOCKET-EMITTER ERROR] Lỗi gửi socket ví: {}", e.getMessage());
        }
    }

    @Override
    public void sendBookingNotification(Long userId, Object bookingPayload) {
        try {
            log.info("[SOCKET-EMITTER] Đang đẩy thông báo booking tới UserId: {}", userId);

            // Sử dụng hằng số tập trung
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    SocketDestinations.BOOKING_QUEUE,
                    bookingPayload
            );
        } catch (Exception e) {
            log.error("[SOCKET-EMITTER ERROR] Lỗi gửi socket booking: {}", e.getMessage());
        }
    }

    @Override
    public void broadcastRoomStatus(String subTopic, Object payload) {
        try {
            // Tự động ghép nối tiền tố /topic/ + tên subTopic (Ví dụ: /topic/room-status)
            String finalDestination = SocketDestinations.TOPIC_PREFIX + subTopic;
            log.info("[SOCKET-EMITTER] Phát tín hiệu Broadcast tới đích: {}", finalDestination);

            messagingTemplate.convertAndSend(finalDestination, payload);
        } catch (Exception e) {
            log.error("[SOCKET-EMITTER ERROR] Lỗi phát tín hiệu Broadcast: {}", e.getMessage());
        }
    }

    @Override
    public void sendNotification(Long userId, Object notificationPayload) {
        try {
            log.info("[SOCKET-EMITTER] Đang đẩy thông báo tới UserId: {}", userId);

            // THÊM .build() VÀO ĐÂY!
            var response = ApiResponse.builder()
                    .data(notificationPayload)
                    .code(ResultCode.SUCCESS.getCode())
                    .build();

            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    SocketDestinations.NOTIFICATION_QUEUE,
                    response
            );
        } catch (Exception e) {
            log.error("[SOCKET-EMITTER ERROR] Lỗi gửi socket : ", e);
        }
    }
}