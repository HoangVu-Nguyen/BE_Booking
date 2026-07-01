package clyvasync.Clyvasync.modules.homestay.entity;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface HomestayRepositorys extends JpaRepository<Homestay, Long> {

    // Tìm homestay theo location + category + trạng thái active, chưa bị xóa
    @Query("SELECT h FROM Homestay h WHERE " +
            "(:locationId IS NULL OR h.locationId = :locationId) AND " +
            "(:categoryId IS NULL OR h.categoryId = :categoryId) AND " +
            "h.status = :status AND h.deletedAt IS NULL")
    List<Homestay> searchByLocationAndCategory(
            @Param("locationId") Integer locationId,
            @Param("categoryId") Integer categoryId,
            @Param("status") HomestayStatus status);

    // Tìm homestay có rating từ mức tối thiểu trở lên (VD: lọc "chỉ hiện 4 sao trở lên")
    List<Homestay> findByAverageRatingGreaterThanEqualAndStatusAndDeletedAtIsNull(
            BigDecimal minRating, HomestayStatus status);

    // Lấy danh sách homestay theo owner (chủ nhà xem homestay của mình)
    List<Homestay> findByOwnerIdAndDeletedAtIsNull(Long ownerId);

    // Tìm homestay theo tên (search gần đúng, không phân biệt hoa thường)
    List<Homestay> findByNameContainingIgnoreCaseAndDeletedAtIsNull(String keyword);
}
