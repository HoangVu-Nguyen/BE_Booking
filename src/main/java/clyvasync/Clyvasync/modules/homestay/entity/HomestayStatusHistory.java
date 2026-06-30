package clyvasync.Clyvasync.modules.homestay.entity;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "homestay_status_history")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomestayStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long homestayId;
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private HomestayStatus oldStatus;
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private HomestayStatus newStatus;
    private String changedBy;
    private String reason;
    private LocalDateTime createdAt;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}