package clyvasync.Clyvasync.repository.booking;

import clyvasync.Clyvasync.dto.projection.*;
import clyvasync.Clyvasync.dto.response.PastTripResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findBookingByBookingCode(String bookingCode);
    List<Booking> findAllByStatusAndCreatedAtBefore(BookingStatus status, OffsetDateTime createdAt);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Booking> findByBookingCodeAndUserId(String bookingCode, Long currentUserId);
    @Query("SELECT new clyvasync.Clyvasync.dto.response.PastTripResponse(" +
            "b.id, b.bookingCode, h.name, img.imageUrl, loc.cityName, " +
            "TO_CHAR(b.updatedAt, 'Month, YYYY'), h.averageRating, " +
            "CASE WHEN r.id IS NOT NULL THEN 'REVIEWED' ELSE 'NOT_REVIEWED' END) " +
            "FROM Booking b " +
            "JOIN Homestay h ON b.homestayId = h.id " +
            "LEFT JOIN HomestayImage img ON h.id = img.homestayId AND img.isPrimary = true " +
            "LEFT JOIN Location loc ON h.locationId = loc.id " +
            "LEFT JOIN Review r ON b.id = r.bookingId " +
            "WHERE b.userId = :userId AND b.status = 'COMPLETED' " +
            "ORDER BY b.updatedAt DESC")
    List<PastTripResponse> findPastTripsByUserId(@Param("userId") Long userId);
    List<Booking> findAllByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, BookingStatus status);
    @Query("SELECT new clyvasync.Clyvasync.dto.projection.BookingTimelineProjection(" +
            "b.userId,bd.roomId, b.id, b.guestName, bd.checkInDate, bd.checkOutDate, b.status,bd.quantity) " +
            "FROM BookingDetail bd, Booking b " +
            "WHERE bd.bookingId = b.id " +
            "AND bd.roomId IN :roomIds " +
            "AND b.status NOT IN ('CANCELLED', 'REJECTED') " +
            "AND bd.checkInDate <= :endDate AND bd.checkOutDate >= :startDate")
    List<BookingTimelineProjection> findOverlappingBookings(
            @Param("roomIds") List<Long> roomIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("SELECT b FROM Booking b JOIN BookingDetail bd ON b.id = bd.bookingId " +
            "WHERE b.payoutStatus = 'ON_HOLD' " +
            "AND b.status NOT IN ('DISPUTE', 'CANCELLED', 'FAILED') " +
            "AND bd.checkInDate <= :targetDate")
    List<Booking> findBookingsReadyForEscrowRelease(@Param("targetDate") LocalDate targetDate);
    List<Booking> findByHomestayIdInOrderByCreatedAtDesc(List<Long> homestayIds);
    @Query("SELECT b FROM Booking b WHERE " +
            "(b.status = :draftStatus AND b.createdAt < :draftThreshold) OR " +
            "(b.status = :paymentStatus AND b.updatedAt < :paymentThreshold)")
    List<Booking> findAllExpired(
            @Param("draftThreshold") OffsetDateTime draftThreshold,
            @Param("paymentThreshold") OffsetDateTime paymentThreshold,
            @Param("draftStatus") BookingStatus draftStatus,
            @Param("paymentStatus") BookingStatus paymentStatus
    );
    @Query("""
        SELECT COALESCE(SUM(b.totalPrice), 0) 
        FROM Booking b 
        WHERE b.homestayId IN :homestayIds 
          AND b.status IN ('CONFIRMED', 'COMPLETED') 
          AND b.createdAt >= :startDate
    """)
    BigDecimal sumRevenueByHomestays(
            @Param("homestayIds") List<Long> homestayIds,
            @Param("startDate")OffsetDateTime startDate
    );
    @Query("""
        SELECT COALESCE(SUM(b.hostPayoutAmount), 0) 
        FROM Booking b 
        WHERE b.homestayId IN :homestayIds 
          AND b.status IN ('CONFIRMED', 'COMPLETED') 
          AND b.createdAt >= :startDate 
          AND b.createdAt <= :endDate
    """)
    BigDecimal sumRevenueByHomestaysAndDateRange(
            @Param("homestayIds") List<Long> homestayIds,
            @Param("startDate") java.time.OffsetDateTime startDate,
            @Param("endDate") java.time.OffsetDateTime endDate
    );
    @Query(value = """
    SELECT b.* FROM bookings b 
    INNER JOIN homestays h ON b.homestay_id = h.id
    WHERE b.user_id = :userId 
      AND h.owner_id = :hostId 
      AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
    ORDER BY b.created_at DESC 
    LIMIT 1
""", nativeQuery = true)
    Optional<Booking> findLatestActiveHomestayBooking(@Param("userId") Long userId, @Param("hostId") Long hostId);
    @Query(value = """
        SELECT b.id AS bookingId, 
               b.booking_code AS bookingCode, 
               h.name AS homestayName, 
               b.status AS status, 
               bd.check_in_date AS checkIn, 
               bd.check_out_date AS checkOut, 
               b.total_price AS totalPrice
        FROM bookings b
        JOIN homestays h ON b.homestay_id = h.id
        JOIN booking_details bd ON bd.booking_id = b.id
        -- SỬA Ở ĐÂY: Tìm cả 2 chiều (Khách -> Chủ HOẶC Chủ -> Khách)
        WHERE ((b.user_id = :userId AND h.owner_id = :hostId) 
           OR  (b.user_id = :hostId AND h.owner_id = :userId))
          AND b.status IN ('PENDING', 'CONFIRMED', 'CHECKED_IN')
        ORDER BY b.created_at DESC 
        LIMIT 1
    """, nativeQuery = true)
    Optional<BookingBriefProjection> findLatestBookingBrief(@Param("userId") Long userId, @Param("hostId") Long hostId);
    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") BookingStatus status);
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status NOT IN ('CANCELLED', 'REJECTED', 'REFUNDED')")
    BigDecimal sumTotalGmv();

    @Query("SELECT SUM(b.platformFeeAmount) FROM Booking b WHERE b.status NOT IN ('CANCELLED', 'REJECTED', 'REFUNDED')")
    BigDecimal sumTotalPlatformFee();


    @Query("SELECT h.ownerId as ownerId, " +
            "SUM(CASE WHEN b.status NOT IN ('CANCELLED', 'REJECTED', 'REFUNDED') THEN b.totalPrice ELSE 0 END) as gmv, " +
            "SUM(CASE WHEN b.status NOT IN ('CANCELLED', 'REJECTED', 'REFUNDED') THEN b.platformFeeAmount ELSE 0 END) as platformFee, " +
            "COUNT(b.id) as totalBookingsAllStatus, " +
            "SUM(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedBookings, " +
            "SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledBookings " +
            "FROM Booking b JOIN Homestay h ON b.homestayId = h.id " +
            "WHERE h.ownerId IN :ownerIds " +
            "GROUP BY h.ownerId")
    List<HostFinancialProjection> sumFinancialMetricsByOwners(@Param("ownerIds") List<Long> ownerIds);
    @Query("SELECT b.homestayId as homestayId, " +
            "COUNT(b.id) as totalBookingsAllStatus, " +
            "SUM(CASE WHEN b.status = 'COMPLETED' THEN 1 ELSE 0 END) as completedBookings, " +
            "SUM(CASE WHEN b.status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelledBookings, " +
            "SUM(CASE WHEN b.status NOT IN ('CANCELLED', 'REJECTED', 'REFUNDED') THEN b.totalPrice ELSE 0 END) as totalRevenue " +
            "FROM Booking b WHERE b.homestayId IN :homestayIds " +
            "GROUP BY b.homestayId")
    List<HomestayFinancialProjection> getFinancialStatsByHomestayIds(@Param("homestayIds") List<Long> homestayIds);
    Long countByStatus(BookingStatus status);
    @Query("SELECT COALESCE(SUM(b.totalPrice), 0.0) FROM Booking b WHERE DATE(b.createdAt) = :date")
    Double sumTotalPriceByDate(@Param("date") LocalDate date);
    @Query(value = "SELECT \n" +
            "    TO_CHAR(d.date, 'Dy') as day, \n" +
            "    COALESCE(SUM(b.total_price), 0) as value \n" +
            "FROM generate_series(CURRENT_DATE - 6, CURRENT_DATE, '1 day'::interval) d\n" +
            "LEFT JOIN bookings b ON DATE(b.created_at) = d.date\n" +
            "GROUP BY d.date\n" +
            "ORDER BY d.date;", nativeQuery = true)
    List<RevenueProjection> getRevenueLast7Days();
    @Query(value = """
    SELECT 
        CASE 
            WHEN :type = 'WEEK' THEN 'Tuần ' || TO_CHAR(created_at, 'IW')
            WHEN :type = 'MONTH' THEN TO_CHAR(created_at, 'YYYY-MM')
            WHEN :type = 'QUARTER' THEN TO_CHAR(created_at, 'YYYY') || '-Q' || EXTRACT(QUARTER FROM created_at)
            WHEN :type = 'YEAR' THEN TO_CHAR(created_at, 'YYYY')
        END as timeLabel,
        SUM(total_price) as gmv,
        SUM(platform_fee_amount) as revenue
    FROM bookings
    WHERE status = 'CONFIRMED' 
      AND created_at >= :startDate
    GROUP BY timeLabel
    ORDER BY MIN(created_at) ASC
    """, nativeQuery = true)
    List<RevenueProjection> getRevenueReport(@Param("type") String type,
                                             @Param("startDate") LocalDateTime startDate);

    @Query(value = """
        SELECT 
            CAST(EXTRACT(MONTH FROM b.created_at) AS INTEGER) as month, 
            COALESCE(SUM(b.host_payout_amount), 0) as revenue
        FROM bookings b
        JOIN homestays h ON b.homestay_id = h.id
        WHERE h.owner_id = :hostId
          AND EXTRACT(YEAR FROM b.created_at) = :year
          AND b.status IN ('COMPLETED', 'CONFIRMED', 'CHECKED_IN')
        GROUP BY EXTRACT(MONTH FROM b.created_at)
    """, nativeQuery = true)
    List<MonthlyRevenueProjection> getMonthlyRevenueByHostAndYear(@Param("hostId") Long hostId, @Param("year") int year);
}
