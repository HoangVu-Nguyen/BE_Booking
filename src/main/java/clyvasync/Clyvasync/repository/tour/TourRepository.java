package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.enums.type.TourStatus;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour,Long> {
    List<Tour> findByHomestayId(Long homestayId);

     List<Tour> findByHomestayIdAndStatus(Long homestayId, TourStatus status);
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN FETCH t.images " +
            "WHERE t.homestayId = :homestayId AND t.allowExternalGuests = true AND t.status = 'ACTIVE'")
    List<Tour> findExternalTours(@Param("homestayId") Long homestayId);

    @Query("SELECT t FROM Tour t WHERE " +
            "(:query IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:homestayId IS NULL OR t.homestayId = :homestayId) AND " +
            "(t.status = :status) AND " +
            "(t.allowExternalGuests = true)")
    Page<Tour> searchTours(@Param("query") String query,
                           @Param("homestayId") Long homestayId,
                           @Param("status") TourStatus status,
                           Pageable pageable);
    @Query(value = "SELECT DISTINCT t FROM Tour t LEFT JOIN FETCH t.images",
            countQuery = "SELECT count(t) FROM Tour t")
    Page<Tour> findAllTours(Pageable pageable);
}
