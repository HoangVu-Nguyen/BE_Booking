package clyvasync.Clyvasync.service.chat.impl;

import clyvasync.Clyvasync.dto.projection.ConversationInboxProjection;
import clyvasync.Clyvasync.dto.request.CreateConversationRequest;
import clyvasync.Clyvasync.dto.response.ConversationDetailResponse;
import clyvasync.Clyvasync.dto.response.ConversationSummaryResponse;
import clyvasync.Clyvasync.enums.type.ChatType;
import clyvasync.Clyvasync.repository.chat.ConversationRepository;
import clyvasync.Clyvasync.service.chat.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
    @Override
    public Page<ConversationSummaryResponse> getUserConversations(Long userId, String filterTab, String searchQuery, Pageable pageable) {
        Page<ConversationInboxProjection> rawPage = conversationRepository.findInboxByUserIdNative(userId, searchQuery, pageable);

        return rawPage.map(proj -> {

            OffsetDateTime rawLastMessageAt = proj.getLastMessageAt() != null
                    ? proj.getLastMessageAt().toInstant().atOffset(ZoneOffset.UTC)
                    : null;

            ChatType type = proj.getChatType() != null
                    ? ChatType.valueOf(proj.getChatType())
                    : ChatType.HOST;

            String targetName = proj.getTargetName() != null
                    ? proj.getTargetName()
                    : "Khách hàng ẩn danh";

            Long unreadCount = proj.getUnreadCount() != null ? proj.getUnreadCount() : 0L;

            return ConversationSummaryResponse.builder()
                    .id(proj.getConversationId())
                    .type(type)
                    .targetName(targetName)
                    .targetAvatar(proj.getTargetAvatar())
                    .lastMessage(proj.getLastMessageContent())
                    .lastMessageTime(formatTimeFriendly(rawLastMessageAt))
                    .unreadCount(unreadCount)
                    .bookingStatus(proj.getBookingStatus())
                    .propertyName(proj.getPropertyName())
                    .build();
        });
    }


    @Override
    public ConversationDetailResponse getConversationDetail(Long conversationId, Long userId) {
        return null;
    }

    @Override
    public Long getOrCreateHostConversation(Long guestId, Long hostId, Long bookingId) {
        return 0L;
    }

    @Override
    public Long createGroupConversation(CreateConversationRequest request) {
        return 0L;
    }

    @Override
    public void updateLastMessageAt(Long conversationId, OffsetDateTime lastMessageAt) {
    conversationRepository.updateLastMessageAt(conversationId,lastMessageAt);
    }

    @Override
    public void markAsRead(Long conversationId, Long userId, Long lastReadMessageId) {

    }

    @Override
    public long getTotalUnreadCount(Long userId) {
        return 0;
    }
    /**
     * Helper method to format OffsetDateTime into user-friendly strings.
     */
    private String formatTimeFriendly(OffsetDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }

        java.time.ZonedDateTime zonedDateTime = dateTime.atZoneSameInstant(ZoneId.systemDefault());
        java.time.LocalDate date = zonedDateTime.toLocalDate();
        java.time.LocalDate today = java.time.LocalDate.now();

        if (date.equals(today)) {
            return TIME_FORMATTER.format(zonedDateTime);
        } else if (date.equals(today.minusDays(1))) {
            return "Hôm qua";
        } else {
            return DATE_FORMATTER.format(zonedDateTime);
        }
    }
}
