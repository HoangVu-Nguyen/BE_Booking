package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long>, JpaSpecificationExecutor<Homestay> {
    @Query("SELECT h FROM Homestay h " +
        "WHERE h.status = 'AVAILABLE' " +
        "AND h.maxGuests >= :guestCount " +
        "AND h.id NOT IN (" +
        "SELECT b.homestayId FROM Booking b " +
        "WHERE b.status IN ('CONFIRMED', 'PENDING') " +
        "AND b.checkInDate < :endDate " +
        "AND b.checkOutDate > :startDate" +
        ")")
List<Homestay> findAvailableHomestays(
                @Param("startDate") LocalDate startDate,
                @Param("endDate") LocalDate endDate,
                @Param("guestCount") int guestCount
        );


}