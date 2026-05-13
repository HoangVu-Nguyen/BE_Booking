package clyvasync.Clyvasync.repository.booking;

import clyvasync.Clyvasync.modules.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query("SELECT b FROM Booking b " +
            "WHERE b.homestayId = :homestayId " +
            "AND b.status IN :statuses " +
            "AND b.checkInDate < :endDate " +
            "AND b.checkOutDate > :startDate")
    List<Booking> findOverlappingBookings(
            @Param("homestayId") Long homestayId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<String> statuses
    );

}
