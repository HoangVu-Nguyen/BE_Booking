package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "homestay_price_calendar")
@Getter
@Setter
@NoArgsConstructor
public class HomestayPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id")
    private Long homestayId;

    private java.time.LocalDate calendarDate;
    private BigDecimal price;
    private Boolean isAvailable = true;
}