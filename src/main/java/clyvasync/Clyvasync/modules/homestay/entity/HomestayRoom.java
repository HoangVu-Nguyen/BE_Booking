package clyvasync.Clyvasync.modules.homestay.entity;

import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.enums.room.RoomType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "homestay_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class HomestayRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id", nullable = false)
    private Long homestayId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String tag;

    @Column(length = 50)
    private String area;

    @Column(length = 50)
    private String floor;

    @Column(length = 50)
    private String wing;

    @Builder.Default
    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests = 2;

    @Builder.Default
    @Column(name = "bed_count", nullable = false)
    private Integer bedCount = 1;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "image_url", length = 500)
    private String imageUrl;
    @Column(name = "is_instant_book")
    private Boolean isInstantBook = true;

    @Enumerated(EnumType.STRING)
    private RoomStatus status = RoomStatus.ACTIVE;
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private RoomType type;

    @Column(name = "has_private_bathroom")
    private Boolean hasPrivateBathroom = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}