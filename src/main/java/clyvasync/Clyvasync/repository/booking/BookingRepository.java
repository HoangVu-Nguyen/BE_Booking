package clyvasync.Clyvasync.repository.booking;

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
}
