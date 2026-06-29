package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.dto.projection.HostPropertyStatsProjection;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
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
    List<Homestay> findByIdIn(List<Long> ids);
    List<Homestay> findAllByOwnerId(Long ownerId);
    @Query("select h.ownerId from Homestay as h where h.id =  :homestayId ")
    Long getOwnerIdByHomestayId(Long homestayId);
    boolean existsByIdAndOwnerId(Long homestayId, Long ownerId);
    List<Homestay> findByStatus(HomestayStatus status);
    List<Homestay> findByStatusIn(List<HomestayStatus> statuses);
    @Query("SELECT h.ownerId AS ownerId, " +
            "COUNT(DISTINCT h.id) AS totalProperties, " +
            "SUM(CASE WHEN h.status IN :pendingStatuses THEN 1 ELSE 0 END) AS pendingProperties " +
            "FROM Homestay h " +
            "LEFT JOIN HomestayDocument hd ON h.id = hd.homestayId " +
            "WHERE h.ownerId IN :ownerIds " +
            "GROUP BY h.ownerId")
    List<HostPropertyStatsProjection> getPropertyStatsByOwners(
            @Param("ownerIds") List<Long> ownerIds,
            @Param("pendingStatuses") List<HomestayStatus> pendingStatuses
    );
    List<Homestay> findByOwnerIdAndStatusIn(Long ownerId, List<HomestayStatus> statuses);

}