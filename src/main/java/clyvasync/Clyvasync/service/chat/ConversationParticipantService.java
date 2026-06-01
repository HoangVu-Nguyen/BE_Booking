package clyvasync.Clyvasync.service.chat;

import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ConversationParticipantService {
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
    Optional<Long> findExistingConversationId(Long userId1,  Long userId2);
}
