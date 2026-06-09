package clyvasync.Clyvasync.modules.chat.entity;

import clyvasync.Clyvasync.enums.media.MediaStatus;
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

    // ĐÃ SỬA: Bỏ nullable = false đi
    @Column(name = "message_id")
    private Long messageId;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private MediaStatus status;

    @Column(updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;
}