package clyvasync.Clyvasync.modules.homestay.entity;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "homestays")
@Getter
@Setter
@NoArgsConstructor
public class Homestay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String address;
    private String city;
    @Column(name = "latitude", precision = 10, scale = 8)
    private java.math.BigDecimal latitude;

    @Column(name = "longitude", precision = 11, scale = 8)
    private java.math.BigDecimal longitude;


    private BigDecimal basePrice;
    private Integer maxGuests;
    private Integer numBedrooms;
    private Integer numBathrooms;

    @Enumerated(EnumType.STRING)
    private HomestayStatus status = HomestayStatus.AVAILABLE;


    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "homestay_id") // Hibernate sẽ dùng cột homestay_id ở bảng homestay_images để JOIN
    private List<HomestayImage> images;

    @ElementCollection
    @CollectionTable(
            name = "homestay_amenities",
            joinColumns = @JoinColumn(name = "homestay_id")
    )
    @Column(name = "amenity_id")
    private Set<Long> amenityIds;

    @Version
    private Integer version;

    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(name = "review_count")
    private Integer reviewCount = 0;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}