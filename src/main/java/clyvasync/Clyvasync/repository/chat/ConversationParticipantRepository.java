package clyvasync.Clyvasync.repository.chat;

import clyvasync.Clyvasync.modules.chat.entity.ConversationParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant,Long> {
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);
    @Query(value = """
        SELECT cp1.conversation_id 
        FROM conversation_participants cp1
        JOIN conversation_participants cp2 ON cp1.conversation_id = cp2.conversation_id
        JOIN conversations c ON c.id = cp1.conversation_id
        WHERE cp1.user_id = :userId1 
          AND cp2.user_id = :userId2 
          AND c.type = 'HOST'
        LIMIT 1
    """, nativeQuery = true)
    Optional<Long> findExistingConversationId(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    @Query(value = """
        SELECT user_id 
        FROM conversation_participants 
        WHERE conversation_id = :conversationId 
          AND user_id != :senderId 
        LIMIT 1
    """, nativeQuery = true)
    Long findReceiverIdByConversationIdAndExcludeSender(
            @Param("conversationId") Long conversationId,
            @Param("senderId") Long senderId
    );

    @Query(value = """
        SELECT user_id 
        FROM conversation_participants 
        WHERE conversation_id = :conversationId
    """, nativeQuery = true)
    List<Long> findAllParticipantIdsByConversationId(@Param("conversationId") Long conversationId);
}
