package clyvasync.Clyvasync.modules.chat.entity;
import clyvasync.Clyvasync.enums.type.ChatType;
import jakarta.persistence.*;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
@Entity
@Table(name = "message_attachments")
@Data
public class MessageAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long messageId;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType;
    @Column(updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;
}