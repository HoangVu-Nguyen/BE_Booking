package clyvasync.Clyvasync.modules.chat.entity;
import clyvasync.Clyvasync.enums.type.ChatType;
import clyvasync.Clyvasync.enums.type.MessageType;
import jakarta.persistence.*;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
@Entity
@Table(name = "messages")
@Data
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long conversationId;

    @Column(nullable = false)
    private Long senderId;

    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MessageType type;
    @Column(updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;
}