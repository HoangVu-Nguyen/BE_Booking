package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "homestay_calendar")
@Getter
@Setter
public class HomestayCalendar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id", nullable = false)
    private Long homestayId;

    @Column(name = "night_date", nullable = false)
    private LocalDate nightDate;

    @Column(name = "price_override")
    private BigDecimal priceOverride;

    @Column(name = "is_available")
    private Boolean isAvailable = true;
}