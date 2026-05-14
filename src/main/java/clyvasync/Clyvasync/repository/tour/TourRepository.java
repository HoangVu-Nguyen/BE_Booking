package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.enums.type.TourStatus;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour,Long> {
    List<Tour> findAllByHomestayIdAndStatus(Long homestayId, TourStatus status);

    // Nếu sau này bác muốn lấy theo lô cho trang Search Homestay
    List<Tour> findAllByHomestayIdInAndStatus(List<Long> homestayIds, TourStatus status);
    @Query(value = """
        SELECT DISTINCT t.* 
        FROM tours t
        JOIN tour_availability ta ON t.id = ta.tour_id
        WHERE t.homestay_id = :homestayId
          AND t.status = 'ACTIVE'
          AND ta.is_active = true
          AND ta.remaining_slots > 0
          AND ta.start_date >= :checkIn 
          AND ta.start_date <= :checkOut
    """, nativeQuery = true)
    List<Tour> findAvailableToursByDates(
            @Param("homestayId") Long homestayId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );


}
