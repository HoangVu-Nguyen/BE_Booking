package clyvasync.Clyvasync.service.chat;

public interface ConversationParticipantService {
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
}
