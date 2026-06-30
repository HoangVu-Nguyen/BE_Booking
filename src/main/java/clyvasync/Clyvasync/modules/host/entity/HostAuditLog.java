package clyvasync.Clyvasync.modules.host.entity;

import clyvasync.Clyvasync.enums.user.UserStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "host_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private UserStatus action;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}