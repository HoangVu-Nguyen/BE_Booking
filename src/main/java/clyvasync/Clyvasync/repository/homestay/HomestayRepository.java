package clyvasync.Clyvasync.repository.homestay;

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

}