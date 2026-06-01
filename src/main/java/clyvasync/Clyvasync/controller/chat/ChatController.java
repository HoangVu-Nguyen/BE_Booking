package clyvasync.Clyvasync.controller.chat;

import clyvasync.Clyvasync.dto.request.SendMessageRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.ChatHistoryResponse;
import clyvasync.Clyvasync.dto.response.ConversationSummaryResponse;
import clyvasync.Clyvasync.dto.response.MessageResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.chat.ConversationService;
import clyvasync.Clyvasync.service.chat.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final ConversationService conversationService;

    // ==========================================
    // API INBOX (Cột 1)
    // ==========================================

    /**
     * Lấy danh sách phòng chat (Inbox) của User hiện tại.
     * GET /api/v1/chat/conversations?filterTab=ALL&searchQuery=Hải&page=0&size=20
     */
    @GetMapping("/conversations")
    public ApiResponse<Page<ConversationSummaryResponse>> getConversations(
            @RequestParam(defaultValue = "ALL") String filterTab,
            @RequestParam(required = false) String searchQuery,
            @PageableDefault(size = 20) Pageable pageable,
            @CurrentUserId Long currentUserId) {

        Page<ConversationSummaryResponse> result = conversationService.getUserConversations(currentUserId, filterTab, searchQuery, pageable);
        return ApiResponse.success(result);
    }

    /**
     * Lấy tổng số tin nhắn chưa đọc để hiển thị Badge đỏ trên Header/Menu
     * GET /api/v1/chat/unread-count
     */
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@CurrentUserId Long currentUserId) {

        long count = conversationService.getTotalUnreadCount(currentUserId);
        return ApiResponse.success(count);
    }

    // ==========================================
    // API MESSAGES (Cột 2)
    // ==========================================

    /**
     * Lấy lịch sử tin nhắn của 1 phòng chat (Cursor-based Pagination)
     * GET /api/v1/chat/conversations/{conversationId}/messages?cursor=150&limit=20
     */
    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<ChatHistoryResponse> getChatHistory(
            @PathVariable("id") Long conversationId,
            @RequestParam(value = "cursor", required = false) Long cursorMessageId,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @CurrentUserId Long currentUserId) {

        log.info("REST API: User {} fetching chat history for conversation {} with cursor {}",
                currentUserId, conversationId, cursorMessageId);

        return ApiResponse.success(messageService.getChatHistory(
                conversationId,
                cursorMessageId,
                limit,
                currentUserId
        ));
    }

    /**
     * Gửi tin nhắn mới vào phòng chat
     * POST /api/v1/chat/conversations/{conversationId}/messages
     */
    @PostMapping("/conversations/{conversationId}/messages")
    public ApiResponse<MessageResponse> sendMessage(
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request,
            @CurrentUserId Long currentUserId) {

        // Trả về DTO vừa gửi (bao gồm cả ID mới tạo và format thời gian) để Angular push ngay vào mảng messages
        MessageResponse response = messageService.sendMessage(currentUserId, conversationId, request);
        return ApiResponse.success(response);
    }

    /**
     * Đánh dấu đã đọc tin nhắn trong phòng (Gọi API này khi User click vào phòng chat)
     * POST /api/v1/chat/conversations/{conversationId}/read
     */
    @PostMapping("/conversations/{conversationId}/read")
    public ApiResponse<Void> markAsRead(
            @PathVariable Long conversationId,
            @RequestParam Long lastMessageId,
            @CurrentUserId Long currentUserId) {

        conversationService.markAsRead(conversationId, currentUserId, lastMessageId);
        return ApiResponse.success(null); // Có thể trả về null hoặc một chuỗi thông báo tùy theo setup class ApiResponse của bạn
    }

}