package clyvasync.Clyvasync.modules.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tour_availability")
@Getter
@Setter
public class TourAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "remaining_slots", nullable = false)
    private Integer remainingSlots;

    @Column(name = "price_override")
    private BigDecimal priceOverride;

    @Column(name = "is_active")
    private Boolean isActive = true;
}