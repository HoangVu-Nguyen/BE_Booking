package clyvasync.Clyvasync.modules.chat.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "conversation_participants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conversation_user", columnNames = {"conversation_id", "user_id"})
        },
        indexes = {
                @Index(name = "idx_cp_user_id", columnList = "user_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * ID của tin nhắn cuối cùng mà User này đã đọc trong phòng chat.
     * Mặc định là 0. Dùng để đếm Unread Count siêu tốc mà không cần join bảng.
     */
    @Column(name = "last_read_message_id", nullable = false, columnDefinition = "bigint default 0")
    @Builder.Default
    private Long lastReadMessageId = 0L;

    @Column(name = "joined_at", updatable = false)
    @CreationTimestamp
    private OffsetDateTime joinedAt;

}