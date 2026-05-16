package clyvasync.Clyvasync.repository.booking;

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
}
