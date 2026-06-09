package clyvasync.Clyvasync.service.chat.impl;

import clyvasync.Clyvasync.dto.request.AttachmentRequest;
import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.request.SendMessageRequest;
import clyvasync.Clyvasync.dto.request.UploadRequest;
import clyvasync.Clyvasync.dto.response.AttachmentResponse;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;
import clyvasync.Clyvasync.enums.media.MediaStatus;
import clyvasync.Clyvasync.modules.chat.entity.MessageAttachment;
import clyvasync.Clyvasync.repository.chat.MessageAttachmentRepository;
import clyvasync.Clyvasync.service.chat.MessageAttachmentService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.utils.MediaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageAttachmentServiceImpl implements MessageAttachmentService {
    private final MessageAttachmentRepository messageAttachmentRepository;
    private final S3Service s3Service;
    @Override
    public void saveAttachments(Long messageId, List<AttachmentRequest> attachments) {

    }

    @Override
    public List<AttachmentResponse> getAttachmentsByMessageId(Long messageId) {
        return List.of();
    }

    @Override
    public Map<Long, List<AttachmentResponse>> getAttachmentsForMessages(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<MessageAttachment> attachments = messageAttachmentRepository.findByMessageIdIn(messageIds);

        return attachments.stream().collect(Collectors.groupingBy(
                MessageAttachment::getMessageId,
                Collectors.mapping(att -> new AttachmentResponse(att.getId(), att.getFileUrl(), att.getFileType()),
                        Collectors.toList())
        ));
    }

    @Override
    public List<MessageAttachment> saveAll(List<MessageAttachment> messageAttachments) {
        return messageAttachmentRepository.saveAll(messageAttachments) ;
    }

    @Override
    public List<PresignedUrlResponse> prepareBatchUpload(Long userId, BatchUploadRequest batchRequest) {
        List<UploadRequest> items = batchRequest.getItems();
        if (items == null || items.isEmpty()) {
            log.warn("Batch upload request is empty for user: {}", userId);
            return Collections.emptyList();
        }
        log.info("Bắt đầu khởi tạo lô upload gồm {} file cho user: {}", items.size(), userId);
        List<PresignedUrlResponse> responses = new ArrayList<>(items.size());
        List<MessageAttachment> pendingAttachments = new ArrayList<>();
        for (UploadRequest item : items) {
            // 1. Tạo S3 Object Key duy nhất (VD: chat/user_1/1689..._avatar.png)
            String extension = MediaUtil.getFileExtension(item.getFileName());
            String objectKey = MediaUtil.generateObjectKey(userId,item);

            // 2. Gọi S3Service để lấy Presigned URL
            String presignedUrl = s3Service.generatePresignedPutUrl(objectKey, item.getContentType(), item.getFileSize());


            MessageAttachment attachment = new MessageAttachment();
            attachment.setFileUrl(objectKey);
            attachment.setFileType(item.getContentType());
            attachment.setStatus(MediaStatus.PENDING);
            pendingAttachments.add(attachment);

            // 4. Đóng gói Response trả về Frontend
            responses.add(PresignedUrlResponse.builder()
                    .uploadUrl(presignedUrl)
                    .objectKey(objectKey)
                    .build());
        }
        messageAttachmentRepository.saveAll(pendingAttachments);

        return responses;
    }
}
