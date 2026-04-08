package clyvasync.Clyvasync.modules.auth.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_devices", indexes = {
        @Index(name = "idx_user_device_user_id", columnList = "user_id")
})
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refresh_token_id", referencedColumnName = "id")
    private RefreshToken refreshToken;

    @Column(name = "device_name")
    private String deviceName;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "location", columnDefinition = "TEXT")
    private String location;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    // Bổ sung để khớp với SQL
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (lastActive == null) lastActive = LocalDateTime.now();
    }
}