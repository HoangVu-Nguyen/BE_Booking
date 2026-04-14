package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface HomestayRepository extends JpaRepository<Homestay, Long> {

    Optional<Homestay> findByIdAndDeletedAtIsNull(Long id);

    Optional<Homestay> findByIdAndOwnerIdAndDeletedAtIsNull(Long id, Long ownerId);

    List<Homestay> findAllByOwnerIdAndDeletedAtIsNull(Long ownerId);

    @Query(
            value = "SELECT h FROM Homestay h WHERE h.deletedAt IS NULL " +
                    "AND h.status = 'AVAILABLE' " +
                    "AND (:city IS NULL OR LOWER(h.city) LIKE :city) " +
                    "AND (:minPrice IS NULL OR h.basePrice >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR h.basePrice <= :maxPrice) " +
                    "AND (:guests IS NULL OR h.maxGuests >= :guests)",
            // THÊM DÒNG NÀY ĐỂ FIX LỖI:
            countQuery = "SELECT count(h) FROM Homestay h WHERE h.deletedAt IS NULL " +
                    "AND h.status = 'AVAILABLE' " +
                    "AND (:city IS NULL OR LOWER(h.city) LIKE :city) " +
                    "AND (:minPrice IS NULL OR h.basePrice >= :minPrice) " +
                    "AND (:maxPrice IS NULL OR h.basePrice <= :maxPrice) " +
                    "AND (:guests IS NULL OR h.maxGuests >= :guests)"
    )
    Page<Homestay> searchHomestays(
            @Param("city") String city,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("guests") Integer guests,
            Pageable pageable);
}