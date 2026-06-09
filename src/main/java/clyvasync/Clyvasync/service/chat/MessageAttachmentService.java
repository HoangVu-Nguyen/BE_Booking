package clyvasync.Clyvasync.service.chat;


import clyvasync.Clyvasync.dto.request.AttachmentRequest;
import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.request.SendMessageRequest;
import clyvasync.Clyvasync.dto.response.AttachmentResponse;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;
import clyvasync.Clyvasync.modules.chat.entity.MessageAttachment;

import java.util.List;
import java.util.Map;

public interface MessageAttachmentService {

    /**
     * Lưu danh sách file đính kèm (Hình ảnh, Document) cho một tin nhắn.
     * Sử dụng JdbcTemplate Batch Insert hoặc JPA saveAll để tối ưu tốc độ.
     */
    void saveAttachments(Long messageId, List<AttachmentRequest> attachments);

    /**
     * Lấy danh sách file đính kèm của MỘT tin nhắn cụ thể.
     */
    List<AttachmentResponse> getAttachmentsByMessageId(Long messageId);

    /**
     * [QUAN TRỌNG - CHỐNG N+1]
     * Lấy danh sách file đính kèm cho NHIỀU tin nhắn cùng lúc.
     * DB Query: SELECT * FROM message_attachments WHERE message_id IN (1, 2, 3...)
     * * @return Map với Key là messageId, Value là danh sách ảnh của tin nhắn đó.
     */
    Map<Long, List<AttachmentResponse>> getAttachmentsForMessages(List<Long> messageIds);
    List<MessageAttachment> saveAll(List<MessageAttachment> messageAttachments);
    List<PresignedUrlResponse> prepareBatchUpload(Long userId, BatchUploadRequest batchRequest);

}