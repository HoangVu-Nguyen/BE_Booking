package clyvasync.Clyvasync.modules.media.entity;

import clyvasync.Clyvasync.enums.media.ImageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_photos", indexes = {
        // Rất quan trọng: Đánh index để tìm ảnh theo User nhanh hơn
        @Index(name = "idx_user_photo_user_id", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "photo_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ImageType photoType; // 'AVATAR' hoặc 'COVER'

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = false;

    @CreationTimestamp // Tự động gán thời gian khi insert, không cần code tay
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}