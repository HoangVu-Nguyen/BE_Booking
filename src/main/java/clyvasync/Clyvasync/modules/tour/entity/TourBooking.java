package clyvasync.Clyvasync.modules.tour.entity;

import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "tour_bookings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 20)
    private String bookingCode;

    // PHẲNG HÓA: Chỉ lưu ID, không dùng Object Tour
    @Column(name = "tour_id", nullable = false)
    private Long tourId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "homestay_booking_id")
    private Long homestayBookingId;

    // ID của khung giờ khởi hành cụ thể
    @Column(name = "availability_id", nullable = false)
    private Long availabilityId;

    @Column(name = "tour_date", nullable = false)
    private LocalDate tourDate;

    @Column(name = "participant_count", nullable = false)
    private Integer participantCount;

    @Column(name = "total_price", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice;
    @Column(name = "loyalty_points_earned", nullable = false)
    @Builder.Default
    private Integer loyaltyPointsEarned = 0;

    @Enumerated(EnumType.STRING)
    private TourBookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Version
    @Column(nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}