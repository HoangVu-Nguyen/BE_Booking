package clyvasync.Clyvasync.controller.chat;

import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.request.SendMessageRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.chat.ConversationService;
import clyvasync.Clyvasync.service.chat.MessageAttachmentService;
import clyvasync.Clyvasync.service.chat.MessageService;
import clyvasync.Clyvasync.tool.HomestaySearchTool;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;
    private final ConversationService conversationService;
    private final MessageAttachmentService messageAttachmentService;
    private final ChatClient chatClient;
    private final HomestaySearchTool homestaySearchTool;


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
        System.out.println(request);
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
        return ApiResponse.success(null);
    }
    @PostMapping("/conversations/init")
    public ApiResponse<ChatInitResponse> initConversation(
            @RequestParam("targetUserId") Long targetUserId,
            @CurrentUserId Long currentUserId) {


        return ApiResponse.success(conversationService.getHostConversation(currentUserId, targetUserId));
    }
    @PostMapping("/attachments/prepare")
    public ApiResponse<List<PresignedUrlResponse>> prepareAttachments(
            @CurrentUserId Long currentUserId,
            @RequestBody BatchUploadRequest request) {

        log.info("User {} đang xin cấp lệnh upload {} file lên S3", currentUserId, request.getItems().size());
        List<PresignedUrlResponse> responses = messageAttachmentService.prepareBatchUpload(currentUserId, request);

        return ApiResponse.<List<PresignedUrlResponse>>builder()
                .data(responses)
                .code(ResultCode.SUCCESS.getCode())
                .build();
    }
    @PostMapping("/api/chat")
    public String chat(@RequestBody String userMessage) {
        return chatClient.prompt()
                .user(userMessage)
                .tools(homestaySearchTool)
                .call()
                .content();
    }
}