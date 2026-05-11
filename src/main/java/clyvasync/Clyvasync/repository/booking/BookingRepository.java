package clyvasync.Clyvasync.repository.booking;

import clyvasync.Clyvasync.modules.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.userId = :userId " +
            "AND b.homestayId = :homestayId " +
            "AND b.status IN (clyvasync.Clyvasync.enums.booking.BookingStatus.CONFIRMED, " +
            "                 clyvasync.Clyvasync.enums.booking.BookingStatus.PENDING) " +
            "AND b.checkOutDate >= CURRENT_DATE")
    boolean existsActiveBooking(@Param("userId") Long userId, @Param("homestayId") Long homestayId);
}
