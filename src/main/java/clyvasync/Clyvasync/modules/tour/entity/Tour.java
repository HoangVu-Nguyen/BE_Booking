package clyvasync.Clyvasync.modules.tour.entity;

import clyvasync.Clyvasync.enums.type.DurationType;
import clyvasync.Clyvasync.enums.type.TourStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE tours SET deleted_at = CURRENT_TIMESTAMP WHERE id = ? AND version = ?")
@Where(clause = "deleted_at IS NULL")
public class Tour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // LIÊN KẾT MỀM (Soft Reference) sang Homestay Module
    @Column(name = "homestay_id", nullable = false)
    private Long homestayId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", nullable = false)
    private DurationType durationType;

    @Column(name = "duration_value", nullable = false)
    private Integer durationValue;

    @Column(name = "price_per_person", nullable = false, precision = 19, scale = 2)
    private BigDecimal pricePerPerson;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "allow_external_guests")
    private Boolean allowExternalGuests;

    @Enumerated(EnumType.STRING)
    private TourStatus status;

    // QUAN HỆ CỨNG (Hard Reference) trong cùng Module
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TourImage> images = new ArrayList<>();

    // Optimistic Locking
    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public void addImage(TourImage image) {
        images.add(image);
        image.setTour(this);
    }

    public void removeImage(TourImage image) {
        images.remove(image);
        image.setTour(null);
    }
}