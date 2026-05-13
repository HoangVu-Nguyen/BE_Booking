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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tours")
@Getter @Setter
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id", nullable = false)
    private Long homestayId;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "location_detail")
    private String locationDetail;

    @Column(name = "duration_type", nullable = false)
    private String durationType; // HOURS, DAYS

    @Column(name = "duration_value", nullable = false)
    private Integer durationValue;

    @Column(name = "price_per_person", nullable = false)
    private BigDecimal pricePerPerson;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "allow_external_guests")
    private Boolean allowExternalGuests = false;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private TourStatus status = TourStatus.ACTIVE;

    @Version
    private Integer version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}