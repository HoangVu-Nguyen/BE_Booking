package clyvasync.Clyvasync.service.realtime;

import clyvasync.Clyvasync.dto.detail.WalletNotificationPayload;

public interface SocketEmitterService {
    /**
     * Bắn thông báo biến động số dư/trạng thái rút tiền đích danh cho một Host
     */
    void sendWalletNotification(Long ownerId, WalletNotificationPayload payload);

    /**
     * Bắn thông báo thời gian thực về tình trạng đặt phòng (Dùng cho cả Khách và Host sau này)
     */
    void sendBookingNotification(Long userId, Object bookingPayload);

    /**
     * Bắn thông báo công khai cho toàn bộ các Client đang kết nối (Broadcast)
     * Ví dụ: Realtime khóa ngày trên Lịch phòng khi có người đang thanh toán
     */
    void broadcastRoomStatus(String topic, Object payload);
    void sendNotification(Long userId, Object bookingPayload);
}
