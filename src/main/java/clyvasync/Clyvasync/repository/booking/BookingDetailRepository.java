package clyvasync.Clyvasync.repository.booking;

import clyvasync.Clyvasync.dto.projection.BookingCalendarProjection;
import clyvasync.Clyvasync.dto.projection.BookingTimelineProjection;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingDetailRepository extends JpaRepository <BookingDetail,Long> {
    @Query("""
    SELECT bd
    FROM BookingDetail bd 
    JOIN Booking b ON b.id = bd.bookingId
    WHERE bd.roomId = :roomId 
      AND b.status IN ('CONFIRMED', 'PENDING')
      AND bd.checkInDate <= :endOfMonth 
      AND bd.checkOutDate >= :startOfMonth
""")
    List<BookingDetail> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );
    Optional<BookingDetail> findBookingDetailByBookingId(Long bookingId);
    List<BookingDetail> findByBookingIdIn(List<Long> bookingIds);
    List<BookingDetail> findByBookingId(Long bookingId);
    @Query("""
    SELECT new clyvasync.Clyvasync.dto.projection.BookingTimelineProjection(
       b.userId, bd.roomId, b.id, b.guestName, bd.checkInDate, bd.checkOutDate, b.status
    )
    FROM BookingDetail bd 
    JOIN Booking b ON b.id = bd.bookingId
    WHERE bd.roomId IN :roomIds 
      AND b.status IN ('CONFIRMED', 'PENDING')
      AND bd.checkInDate <= :endOfMonth 
      AND bd.checkOutDate >= :startOfMonth
""")
    List<BookingTimelineProjection> findOverlappingBookings(
            @Param("roomIds") List<Long> roomIds,
            @Param("startOfMonth") LocalDate startOfMonth,
            @Param("endOfMonth") LocalDate endOfMonth
    );
    @Query("SELECT bd FROM BookingDetail bd " +
            "JOIN Booking b ON bd.bookingId = b.id " + // Join tay bằng Soft Reference ID
            "WHERE bd.roomId IN :roomIds " +
            "AND b.status NOT IN ('CANCELLED', 'DRAFT') " +
            "AND bd.checkInDate <= :endDate " +
            "AND bd.checkOutDate >= :startDate")
    List<BookingDetail> findActiveBookingsByRoomIdsAndDateRange(
            @Param("roomIds") List<Long> roomIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
    @Query("SELECT bd.roomId AS roomId, " +
            "bd.checkInDate AS checkInDate, " +
            "bd.checkOutDate AS checkOutDate, " +
            "bd.quantity AS quantity, " +
            "b.bookingCode AS bookingCode, " +
            "b.guestName AS guestName " +
            "FROM BookingDetail bd " +
            "JOIN Booking b ON bd.bookingId = b.id " +
            "WHERE bd.roomId IN :roomIds " +
            "AND b.status NOT IN ('CANCELLED', 'DRAFT') " +
            "AND bd.checkInDate <= :endDate " +
            "AND bd.checkOutDate >= :startDate")
    List<BookingCalendarProjection> findActiveBookingsForCalendar(
            @Param("roomIds") List<Long> roomIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
