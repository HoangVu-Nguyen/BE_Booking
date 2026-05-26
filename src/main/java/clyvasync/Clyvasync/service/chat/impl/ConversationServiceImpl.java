package clyvasync.Clyvasync.service.chat.impl;

import clyvasync.Clyvasync.dto.request.CreateConversationRequest;
import clyvasync.Clyvasync.dto.response.ConversationDetailResponse;
import clyvasync.Clyvasync.dto.response.ConversationSummaryResponse;
import clyvasync.Clyvasync.repository.chat.ConversationRepository;
import clyvasync.Clyvasync.service.chat.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {
    private final ConversationRepository conversationRepository;
    @Override
    public Page<ConversationSummaryResponse> getUserConversations(Long userId, String filterTab, String searchQuery, Pageable pageable) {
        return null;
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
}
