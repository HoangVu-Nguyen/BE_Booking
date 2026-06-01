package clyvasync.Clyvasync.repository.chat;

import clyvasync.Clyvasync.modules.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message,Long> {
    @Query("""
        SELECT m FROM Message m 
        WHERE m.conversationId = :conversationId 
          AND (:cursorId IS NULL OR m.id < :cursorId) 
        ORDER BY m.id DESC
    """)
    List<Message> findChatHistoryWithCursor(
            @Param("conversationId") Long conversationId,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
