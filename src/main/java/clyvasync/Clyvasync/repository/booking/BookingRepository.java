package clyvasync.Clyvasync.repository.booking;

import clyvasync.Clyvasync.dto.projection.BookingTimelineProjection;
import clyvasync.Clyvasync.dto.response.PastTripResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findBookingByBookingCode(String bookingCode);
    List<Booking> findAllByStatusAndCreatedAtBefore(String status, OffsetDateTime createdAt);
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
    List<Booking> findAllByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, String status);
    @Query("SELECT new clyvasync.Clyvasync.dto.projection.BookingTimelineProjection(" +
            "b.userId,bd.roomId, b.id, b.guestName, bd.checkInDate, bd.checkOutDate, b.status) " +
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
}
