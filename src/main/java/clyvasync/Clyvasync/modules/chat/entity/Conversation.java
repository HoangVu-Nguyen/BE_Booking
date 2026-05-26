package clyvasync.Clyvasync.modules.chat.entity;

import clyvasync.Clyvasync.enums.type.ChatType;
import jakarta.persistence.*;
import lombok.Data;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "conversations")
@Data
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ChatType type;

    private Long referenceId;

    private String name;

    private OffsetDateTime lastMessageAt;

    @Column(updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;


}