package clyvasync.Clyvasync.service.chat.impl;

import clyvasync.Clyvasync.dto.event.ChatMessageSentEvent;
import clyvasync.Clyvasync.dto.request.SendMessageRequest;
import clyvasync.Clyvasync.dto.response.AttachmentResponse;
import clyvasync.Clyvasync.dto.response.ChatHistoryResponse;
import clyvasync.Clyvasync.dto.response.MessageResponse;
import clyvasync.Clyvasync.dto.response.OwnerResponse;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.chat.entity.Message;
import clyvasync.Clyvasync.modules.chat.entity.MessageAttachment;
import clyvasync.Clyvasync.repository.chat.ConversationParticipantRepository;
import clyvasync.Clyvasync.repository.chat.MessageRepository;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.chat.ConversationParticipantService;
import clyvasync.Clyvasync.service.chat.ConversationService;
import clyvasync.Clyvasync.service.chat.MessageAttachmentService;
import clyvasync.Clyvasync.service.chat.MessageService;
import clyvasync.Clyvasync.service.media.IUserPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final ConversationParticipantService conversationParticipantService;
    private final MessageAttachmentService messageAttachmentService;
    private final ConversationService conversationService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final ConversationParticipantRepository conversationParticipantRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
    @Override
    @Transactional
    public MessageResponse sendMessage(Long senderId, Long conversationId, SendMessageRequest request) {
        log.info("User {} sending message to conversation {}", senderId, conversationId);
        if (senderId != 0) {
            boolean isParticipant = conversationParticipantService.existsByConversationIdAndUserId(conversationId, senderId);
            if (!isParticipant) {
                log.warn("Security Alert: User {} cố gắng thao tác vào phòng chat {} trái phép!", senderId, conversationId);
                throw new AppException(ResultCode.ACCESS_DENIED);
            }
        }
        // 2. Lưu Message vào Database
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setContent(request.getContent());
        message.setType(request.getType());
        Message savedMessage = messageRepository.save(message);

        // 3. Xử lý Attachments (nếu có) bằng cách Batch Save
        List<AttachmentResponse> attachmentResponses = new ArrayList<>();
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            List<MessageAttachment> attachments = request.getAttachments().stream().map(req -> {
                MessageAttachment att = new MessageAttachment();
                att.setMessageId(savedMessage.getId());
                att.setFileUrl(req.getFileUrl());
                att.setFileType(req.getFileType());
                return att;
            }).toList();
            messageAttachmentService.saveAll(attachments).forEach(att -> {
                attachmentResponses.add(new AttachmentResponse(att.getId(), att.getFileUrl(), att.getFileType()));
            });
        }
        conversationService.updateLastMessageAt(conversationId, OffsetDateTime.now());

        // 5. Map sang Response DTO
        MessageResponse response = mapToResponse(savedMessage, senderId, attachmentResponses);
        if (senderId == 0) {
            // Nếu là tin nhắn SYSTEM, bắn cập nhật Inbox cho TẤT CẢ các thành viên trong phòng
            List<Long> participantIds = conversationParticipantRepository.findAllParticipantIdsByConversationId(conversationId);
            for (Long pId : participantIds) {
                eventPublisher.publishEvent(new ChatMessageSentEvent(conversationId, pId, response));
            }
        } else {
            Long receiverId = conversationParticipantRepository.findReceiverIdByConversationIdAndExcludeSender(conversationId, senderId);

            if (receiverId != null) {
                eventPublisher.publishEvent(new ChatMessageSentEvent(conversationId, receiverId, response));
            }
        }
        return response;
    }

    @Override
    public MessageResponse sendSystemMessage(Long conversationId, String content) {
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public ChatHistoryResponse getChatHistory(Long conversationId, Long cursorMessageId, int limit, Long currentUserId) {
        log.info("Loading chat history for conversation {} by user {}, cursor: {}", conversationId, currentUserId, cursorMessageId);

        Pageable pageable = PageRequest.of(0, limit + 1);
        List<Message> messages = messageRepository.findChatHistoryWithCursor(conversationId, cursorMessageId, pageable);

        if (messages.isEmpty()) {
            return ChatHistoryResponse.builder()
                    .messages(Collections.emptyList())
                    .nextCursor(null)
                    .hasNext(false)
                    .build();
        }

        boolean hasNext = messages.size() > limit;
        if (hasNext) {
            messages = messages.subList(0, limit);
        }


        Long nextCursor = messages.get(messages.size() - 1).getId();

        List<Long> messageIds = messages.stream().map(Message::getId).toList();
        List<Long> senderIds = messages.stream().map(Message::getSenderId).distinct().toList();

        Map<Long, List<AttachmentResponse>> attachmentsMap = messageAttachmentService.getAttachmentsForMessages(messageIds);
        Map<Long, OwnerResponse> ownerResponseMap = userService.getOwnerInfos(senderIds);

        List<MessageResponse> responses = messages.stream().map(msg -> {
            List<AttachmentResponse> attachments = attachmentsMap.getOrDefault(msg.getId(), Collections.emptyList());
            MessageResponse res = mapToResponse(msg, currentUserId, attachments);

            if (msg.getSenderId() == 0) {
                res.setSenderName("Hệ thống");
                res.setSenderAvatar("support_agent");
            } else {
                OwnerResponse owner = ownerResponseMap.get(msg.getSenderId());
                if (owner != null) {
                    res.setSenderName(owner.getFullName());
                    res.setSenderAvatar(owner.getAvatar());
                } else {
                    res.setSenderName("Người dùng Clyvasync");
                    res.setSenderAvatar("https://ui-avatars.com/api/?name=User");
                }
            }
            return res;
        }).collect(Collectors.toList());

        Collections.reverse(responses);

        return ChatHistoryResponse.builder()
                .messages(responses)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
    @Override
    public void revokeMessage(Long messageId, Long userId) {

    }

    @Override
    public List<Message> findChatHistoryWithCursor(Long conversationId, Long cursorId, Pageable pageable) {
        return messageRepository.findChatHistoryWithCursor(conversationId, cursorId, pageable);
    }

    private MessageResponse mapToResponse(Message msg, Long currentUserId, List<AttachmentResponse> attachments) {

        boolean isMine = msg.getSenderId().equals(currentUserId);
        OwnerResponse ownerResponse = userService.getOwnerInfo(currentUserId);

        String friendlyTime = formatTimeFriendly(msg.getCreatedAt());

        return MessageResponse.builder()
                .id(msg.getId())
                .senderId(msg.getSenderId())
                .senderAvatar(ownerResponse.getAvatar())
                .senderName(ownerResponse.getFullName())
                .content(msg.getContent())
                .type(msg.getType())
                .time(friendlyTime)
                .isMine(isMine)
                .attachments(attachments != null ? attachments : Collections.emptyList())
                .build();
    }

    /**
     * Chuyển đổi OffsetDateTime sang định dạng chuỗi thân thiện với người dùng.
     * Ví dụ: "10:30" (nếu trong ngày), "Hôm qua", hoặc "26/05/2026" (nếu cũ hơn)
     */
    private String formatTimeFriendly(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        // Chuyển về ZonedDateTime của System Default để so sánh chính xác với ngày hiện tại
        java.time.ZonedDateTime zonedDateTime = dateTime.atZoneSameInstant(ZoneId.systemDefault());
        java.time.LocalDate date = zonedDateTime.toLocalDate();
        java.time.LocalDate today = java.time.LocalDate.now();

        if (date.equals(today)) {
            // Nếu là hôm nay, chỉ hiển thị giờ:phút (VD: "10:30")
            return TIME_FORMATTER.format(zonedDateTime);
        } else if (date.equals(today.minusDays(1))) {
            // Nếu là hôm qua
            return "Hôm qua";
        } else {
            // Nếu cũ hơn, hiển thị ngày/tháng/năm (VD: "25/05/2026")
            return DATE_FORMATTER.format(zonedDateTime);
        }
    }
}
