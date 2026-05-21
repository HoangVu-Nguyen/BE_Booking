package clyvasync.Clyvasync.modules.booking.entity;

import clyvasync.Clyvasync.enums.booking.BookingStatus;

import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.type.PayoutStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, unique = true, length = 20)
    private String bookingCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "homestay_id", nullable = false)
    private Long homestayId; // Mapping mềm

    // ==========================================
    // THÔNG TIN KHÁCH HÀNG
    // ==========================================
    @Column(name = "guest_name")
    private String guestName;

    @Column(name = "guest_email")
    private String guestEmail;

    @Column(name = "guest_phone", length = 20)
    private String guestPhone;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    // ==========================================
    // DÒNG TIỀN VÀ TRẠNG THÁI (ESCROW MODEL)
    // ==========================================
    @Column(name = "total_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Builder.Default
    @Column(name = "tax_fee", precision = 19, scale = 2)
    private BigDecimal taxFee = BigDecimal.ZERO;

    // CỘT MỚI: Tiền thực nhận của Host (sau khi trừ phí App)
    @Builder.Default
    @Column(name = "host_payout_amount", precision = 19, scale = 2)
    private BigDecimal hostPayoutAmount = BigDecimal.ZERO;

    // CỘT MỚI: Phí nền tảng App giữ lại
    @Builder.Default
    @Column(name = "platform_fee_amount", precision = 19, scale = 2)
    private BigDecimal platformFeeAmount = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private BookingStatus status = BookingStatus.PENDING_PAYMENT; // Đã đổi sang Enum

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50, nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID; // Đã đổi sang Enum

    // CỘT MỚI: Trạng thái giải ngân cho Host
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "payout_status", length = 50, nullable = false)
    private PayoutStatus payoutStatus = PayoutStatus.NOT_APPLICABLE;

    // ==========================================
    // THỜI GIAN THỰC TẾ & KHIẾU NẠI (CRONJOB)
    // ==========================================
    // CỘT MỚI: Lưu giờ khách thực sự check-in để bắt đầu đếm ngược 24h
    @Column(name = "actual_check_in_time")
    private OffsetDateTime actualCheckInTime;

    @Column(name = "actual_check_out_time")
    private OffsetDateTime actualCheckOutTime;

    // CỘT MỚI: Nếu khách ấn nút Báo cáo sự cố, lưu giờ vào đây để đóng băng tiền
    @Column(name = "dispute_raised_at")
    private OffsetDateTime disputeRaisedAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "loyalty_points_earned", nullable = false)
    @Builder.Default
    private Integer loyaltyPointsEarned = 0;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}