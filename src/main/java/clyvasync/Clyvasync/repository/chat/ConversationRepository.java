package clyvasync.Clyvasync.repository.chat;

import clyvasync.Clyvasync.modules.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface ConversationRepository extends JpaRepository<Conversation,Long> {
    @Modifying
    @Query("UPDATE Conversation c SET c.lastMessageAt = :time WHERE c.id = :id")
    void updateLastMessageAt(@Param("id") Long id, @Param("time") OffsetDateTime time);
}
