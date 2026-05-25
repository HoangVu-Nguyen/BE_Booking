package clyvasync.Clyvasync.service.notification;

import clyvasync.Clyvasync.dto.response.NotificationResponse;
import clyvasync.Clyvasync.enums.type.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    // =========================================================================
    // 1. NHÓM HÀM WRITE (Dùng nội bộ trong Backend để tạo thông báo)
    // =========================================================================

    /**
     * Hàm core để tạo và lưu thông báo vào Database.
     * Thường được gọi từ các Service khác (BookingService, PaymentService...)
     * * @param recipientId ID của người nhận (User, Host)
     * @param type        Loại thông báo (BOOKING_SUCCESS, PAYMENT_FAILED...)
     * @param title       Tiêu đề hiển thị
     * @param message     Nội dung chi tiết
     * @param metadata    Map chứa dữ liệu JSON động (bookingId, amount,...)
     */
    void sendNotification(Long recipientId, NotificationType type, String title, String message, Map<String, Object> metadata);

    /**
     * (Tùy chọn) Gửi thông báo cho hàng loạt User cùng lúc (Hệ thống thông báo chung)
     */
    void sendBatchNotifications(List<Long> recipientIds, NotificationType type, String title, String message, Map<String, Object> metadata);


    // =========================================================================
    // 2. NHÓM HÀM READ & UPDATE (Dùng cho Controller cung cấp API cho Frontend)
    // =========================================================================

    /**
     * Lấy danh sách thông báo của một người dùng (Có phân trang)
     * Có thể truyền thêm tham số bộ lọc (isRead, type) nếu Frontend cần lọc theo Tab
     */
    Page<NotificationResponse> getUserNotifications(Long currentUserId, String filter, Pageable pageable);

    /**
     * Đếm số lượng thông báo CHƯA ĐỌC để hiển thị lên badge đỏ ở cái chuông
     */
    long countUnreadNotifications(Long currentUserId);

    /**
     * Đánh dấu 1 thông báo cụ thể là đã đọc
     */
    void markAsRead(Long notificationId, Long currentUserId);

    /**
     * Đánh dấu TẤT CẢ thông báo của user là đã đọc (Nút "Đánh dấu tất cả đã đọc")
     */
    void markAllAsRead(Long currentUserId);
}