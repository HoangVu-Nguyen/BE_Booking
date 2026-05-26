package clyvasync.Clyvasync.repository.tour;

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

}
