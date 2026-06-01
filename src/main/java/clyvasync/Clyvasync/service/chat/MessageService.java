package clyvasync.Clyvasync.service.chat;



import clyvasync.Clyvasync.dto.request.SendMessageRequest;
import clyvasync.Clyvasync.dto.response.ChatHistoryResponse;
import clyvasync.Clyvasync.dto.response.MessageResponse;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.modules.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageService {

    /**
     * Gửi tin nhắn mới (Text, Hình ảnh).
     * Hàm này sẽ: Lưu Message -> Lưu Attachments -> Update Conversation.lastMessageAt -> Bắn WebSocket.
     */
    MessageResponse sendMessage(Long senderId, Long conversationId, SendMessageRequest request);

    /**
     * Gửi tin nhắn hệ thống (System Message).
     * Ví dụ: "Đơn đặt phòng đã được xác nhận", "Chuyến đi đã kết thúc".
     * senderId sẽ được set cứng là 0.
     */
    MessageResponse sendSystemMessage(Long conversationId, String content);

    /**
     * Lấy lịch sử chat (Cursor-based Pagination chuẩn Production).
     * @param conversationId ID phòng chat
     * @param cursorMessageId ID của tin nhắn cũ nhất đang hiển thị trên UI (truyền null nếu mới vào phòng)
     * @param limit Số lượng tin nhắn cần lấy (VD: 20, 50)
     */
     ChatHistoryResponse getChatHistory(Long conversationId, Long cursorMessageId, int limit, Long currentUserId);
    /**
     * Xóa/Thu hồi tin nhắn (Soft Delete).
     * @throws AppException nếu người xóa không phải là người gửi tin nhắn.
     */
    void revokeMessage(Long messageId, Long userId);
    List<Message> findChatHistoryWithCursor(
            Long conversationId,
            Long cursorId,
            Pageable pageable
    );
}