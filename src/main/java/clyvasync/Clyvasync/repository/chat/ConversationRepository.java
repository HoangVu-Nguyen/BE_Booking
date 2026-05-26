package clyvasync.Clyvasync.repository.chat;

import clyvasync.Clyvasync.dto.projection.ConversationInboxProjection;
import clyvasync.Clyvasync.dto.response.ConversationSummaryResponse;
import clyvasync.Clyvasync.modules.chat.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    @Modifying
    @Query("UPDATE Conversation c SET c.lastMessageAt = :time WHERE c.id = :id")
    void updateLastMessageAt(@Param("id") Long id, @Param("time") OffsetDateTime time);
    @Query(value = """
        SELECT 
            c.id AS conversationId,
            c.type AS chatType,
            u.full_name AS targetName,
            up.photo_url AS targetAvatar,
            m.content AS lastMessageContent,
            c.last_message_at AS lastMessageAt,
            (SELECT COUNT(*) FROM messages m2 
             WHERE m2.conversation_id = c.id 
               AND m2.id > cp.last_read_message_id 
               AND m2.sender_id != :userId) AS unreadCount,
            b.status AS bookingStatus,
            h.name AS propertyName
        FROM conversations c
        INNER JOIN conversation_participants cp ON cp.conversation_id = c.id AND cp.user_id = :userId
        LEFT JOIN conversation_participants cp_other ON cp_other.conversation_id = c.id AND cp_other.user_id != :userId
        LEFT JOIN users u ON u.id = cp_other.user_id
        LEFT JOIN user_photos up ON up.user_id = u.id AND up.is_primary = true
        LEFT JOIN messages m ON m.conversation_id = c.id 
             AND m.created_at = (SELECT MAX(created_at) FROM messages WHERE conversation_id = c.id)
        LEFT JOIN bookings b ON b.id = c.reference_id AND c.type IN ('ADMIN', 'HOST')
        LEFT JOIN homestays h ON h.id = b.homestay_id
        WHERE (:searchQuery IS NULL OR LOWER(u.full_name) LIKE LOWER(CONCAT('%', :searchQuery, '%')))
        ORDER BY c.last_message_at DESC
        """,
            countQuery = "SELECT COUNT(*) FROM conversation_participants WHERE user_id = :userId",
            nativeQuery = true)
    Page<ConversationInboxProjection> findInboxByUserIdNative(@Param("userId") Long userId, @Param("searchQuery") String searchQuery, Pageable pageable);
}
