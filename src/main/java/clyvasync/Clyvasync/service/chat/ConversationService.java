package clyvasync.Clyvasync.service.chat;


import clyvasync.Clyvasync.dto.request.CreateConversationRequest;
import clyvasync.Clyvasync.dto.response.ConversationDetailResponse;
import clyvasync.Clyvasync.dto.response.ConversationSummaryResponse;
import clyvasync.Clyvasync.exception.AppException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;

public interface ConversationService {

    /**
     * Lấy danh sách Inbox của User (Host/Guest) để hiển thị ở Cột 1.
     * Có thể lọc theo Tab (ALL / UNREAD) và tìm kiếm (searchQuery).
     */
    Page<ConversationSummaryResponse> getUserConversations(
            Long userId,
            String filterTab,
            String searchQuery,
            Pageable pageable
    );

    /**
     * Lấy thông tin chi tiết của 1 phòng chat (Bao gồm thông tin Booking ở Cột 3).
     * @throws AppException nếu User không nằm trong phòng chat này (Bảo mật).
     */
    ConversationDetailResponse getConversationDetail(Long conversationId, Long userId);

    /**
     * Khởi tạo hoặc lấy phòng chat 1-1 giữa Khách và Chủ nhà.
     * Nếu đã chat về Booking này rồi thì trả về ID cũ, chưa có thì tạo mới.
     */
    Long getOrCreateHostConversation(Long guestId, Long hostId, Long bookingId);

    /**
     * Tạo phòng chat Nhóm (Khi có người book Tour).
     */
    Long createGroupConversation(CreateConversationRequest request);

    /**
     * Cập nhật thời gian có tin nhắn mới nhất để đẩy phòng chat lên đầu danh sách Inbox.
     * (Gọi từ MessageService sau khi lưu tin nhắn thành công).
     */
    void updateLastMessageAt(Long conversationId, OffsetDateTime lastMessageAt);

    /**
     * Đánh dấu User đã đọc tin nhắn đến mốc ID nào (Dùng để tính Unread Count).
     */
    void markAsRead(Long conversationId, Long userId, Long lastReadMessageId);

    /**
     * Đếm tổng số tin nhắn chưa đọc của 1 User (Hiển thị chấm đỏ ở Header/Menu).
     */
    long getTotalUnreadCount(Long userId);
}