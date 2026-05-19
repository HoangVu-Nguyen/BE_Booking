package clyvasync.Clyvasync.repository.booking;

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
        bd.roomId, b.id, b.guestName, bd.checkInDate, bd.checkOutDate, b.status
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
}
