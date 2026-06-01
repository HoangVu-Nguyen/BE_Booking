package clyvasync.Clyvasync.service.chat.impl;

import clyvasync.Clyvasync.dto.request.AttachmentRequest;
import clyvasync.Clyvasync.dto.request.SendMessageRequest;
import clyvasync.Clyvasync.dto.response.AttachmentResponse;
import clyvasync.Clyvasync.modules.chat.entity.MessageAttachment;
import clyvasync.Clyvasync.repository.chat.MessageAttachmentRepository;
import clyvasync.Clyvasync.service.chat.MessageAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageAttachmentServiceImpl implements MessageAttachmentService {
    private final MessageAttachmentRepository messageAttachmentRepository;
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
}
