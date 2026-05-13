package clyvasync.Clyvasync.modules.room;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "room_calendar", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "night_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId; // Mapping mềm

    @Column(name = "night_date", nullable = false)
    private LocalDate nightDate;

    @Column(name = "price_override", precision = 19, scale = 2)
    private BigDecimal priceOverride;

    @Column(name = "available_quantity")
    private Integer availableQuantity;
}