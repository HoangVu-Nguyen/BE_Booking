package clyvasync.Clyvasync.modules.homestay.entity;

import clyvasync.Clyvasync.enums.booking.BookingMode;
import clyvasync.Clyvasync.enums.room.CancellationPolicy;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "homestay_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mỗi homestay chỉ có 1 bộ chính sách.
     */
    @Column(name = "homestay_id", nullable = false, unique = true)
    private Long homestayId;

    /**
     * FE: checkInFrom
     */
    @Builder.Default
    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime = LocalTime.of(14, 0);

    /**
     * FE: checkInTo
     */
    @Builder.Default
    @Column(name = "check_in_until")
    private LocalTime checkInUntil = LocalTime.of(22, 0);

    /**
     * FE: checkOutBefore
     */
    @Builder.Default
    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime = LocalTime.of(12, 0);

    @Column(name = "late_check_in_instruction", columnDefinition = "TEXT")
    private String lateCheckInInstruction;

    @Builder.Default
    @Column(name = "min_nights", nullable = false)
    private Integer minNights = 1;

    @Column(name = "max_nights")
    private Integer maxNights;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "booking_mode", nullable = false, length = 30)
    private BookingMode bookingMode = BookingMode.INSTANT_BOOKING;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false, length = 30)
    private CancellationPolicy cancellationPolicy = CancellationPolicy.FLEXIBLE;

    @Builder.Default
    @Column(name = "allows_children", nullable = false)
    private Boolean allowsChildren = true;

    @Builder.Default
    @Column(name = "allows_pets", nullable = false)
    private Boolean allowsPets = false;

    @Builder.Default
    @Column(name = "allows_smoking", nullable = false)
    private Boolean allowsSmoking = false;

    @Builder.Default
    @Column(name = "allows_parties", nullable = false)
    private Boolean allowsParties = false;

    @Builder.Default
    @Column(name = "quiet_hours_enabled", nullable = false)
    private Boolean quietHoursEnabled = true;

    @Builder.Default
    @Column(name = "quiet_from")
    private LocalTime quietFrom = LocalTime.of(22, 0);

    @Builder.Default
    @Column(name = "quiet_to")
    private LocalTime quietTo = LocalTime.of(6, 0);

    @Builder.Default
    @Column(name = "deposit_required", nullable = false)
    private Boolean depositRequired = false;

    @Column(name = "deposit_amount", precision = 19, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "extra_notes", columnDefinition = "TEXT")
    private String extraNotes;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (checkInTime == null) checkInTime = LocalTime.of(14, 0);
        if (checkInUntil == null) checkInUntil = LocalTime.of(22, 0);
        if (checkOutTime == null) checkOutTime = LocalTime.of(12, 0);

        if (minNights == null) minNights = 1;
        if (bookingMode == null) bookingMode = BookingMode.INSTANT_BOOKING;
        if (cancellationPolicy == null) cancellationPolicy = CancellationPolicy.FLEXIBLE;

        if (allowsChildren == null) allowsChildren = true;
        if (allowsPets == null) allowsPets = false;
        if (allowsSmoking == null) allowsSmoking = false;
        if (allowsParties == null) allowsParties = false;

        if (quietHoursEnabled == null) quietHoursEnabled = true;
        if (depositRequired == null) depositRequired = false;

        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}