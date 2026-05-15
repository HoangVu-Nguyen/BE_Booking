package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourBookingRepository extends JpaRepository<TourBooking,Long> {
    Optional<TourBooking> findByHomestayBookingId(Long homestayBookingId);
    List<TourBooking> findAllByHomestayBookingId(Long homestayBookingId);
}
