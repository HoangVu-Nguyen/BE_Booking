package clyvasync.Clyvasync.modules.booking.entity;


import clyvasync.Clyvasync.enums.booking.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter @Setter @NoArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String bookingCode;

    @Column(name = "homestay_id")
    private Long homestayId;

    @Column(name = "user_id")
    private Long userId;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    private String cancellationReason;

    @Version
    private Integer version;

    private LocalDateTime createdAt = LocalDateTime.now();
}
