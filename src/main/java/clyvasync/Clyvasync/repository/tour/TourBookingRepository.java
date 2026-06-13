package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.dto.projection.TourInfoProjection;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking,Long> {
    Optional<TourBooking> findByHomestayBookingId(Long homestayBookingId);
    List<TourBooking> findAllByHomestayBookingId(Long homestayBookingId);
    List<TourBooking> findByHomestayBookingIdIn(List<Long> homestayBookingIds);
    @Modifying(clearAutomatically = true)
    @Query("UPDATE TourBooking tb SET tb.status = 'CANCELLED' WHERE tb.homestayBookingId = :bookingId")
    void cancelAllByHomestayBookingId(@Param("bookingId") Long bookingId);
    @Query(value = """
    SELECT tb.* FROM tour_bookings tb
    INNER JOIN tours t ON tb.tour_id = t.id
    INNER JOIN homestays h ON t.homestay_id = h.id
    WHERE tb.user_id = :userId
      AND h.owner_id = :hostId
      AND tb.status IN ('PENDING', 'CONFIRMED')
    ORDER BY tb.created_at DESC
    LIMIT 1
""", nativeQuery = true)
    Optional<TourBooking> findLatestActiveTourBooking(@Param("userId") Long userId, @Param("hostId") Long hostId);
    @Query(value = """
    SELECT t.name AS tourName, 
           tb.tour_date AS tourDate, 
           tb.total_price AS price
    FROM tour_bookings tb
    JOIN tours t ON tb.tour_id = t.id
    WHERE tb.homestay_booking_id = :bookingId
""", nativeQuery = true)
    List<TourInfoProjection> findTourInfosByHomestayBookingId(@Param("bookingId") Long bookingId);
    @Query("SELECT t FROM TourBooking t WHERE t.homestayBookingId IN :bookingIds AND t.status = :status")
    List<TourBooking> findAllByHomestayBookingIdInAndStatus(@Param("bookingIds") List<Long> bookingIds, @Param("status") TourBookingStatus status);
    @Modifying
    @Query("UPDATE TourBooking t SET t.status = :status WHERE t.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") TourBookingStatus status);
}
