package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    List<Review> findByHomestayIdOrderByCreatedAtDesc(Long homestayId);
    @Query(value = "SELECT r.*, u.full_name as fullName, up.photo_url as avatarUrl " +
            "FROM reviews r " +
            "JOIN users u ON r.user_id = u.id " +
            "LEFT JOIN user_photos up ON u.id = up.user_id AND up.is_current = true " +
            "WHERE r.homestay_id = :homestayId " +
            "ORDER BY r.created_at DESC",
            nativeQuery = true)
    List<Map<String, Object>> findReviewsWithUserInfo(@Param("homestayId") Long homestayId);
    @Query(value = "SELECT r.id, r.rating, r.comment, r.created_at, r.user_id, " +
            "u.full_name as \"fullName\", " +
            "up.photo_url as \"avatarUrl\" " + // Lấy từ bảng user_photos
            "FROM reviews r " +
            "JOIN users u ON r.user_id = u.id " +
            "LEFT JOIN user_photos up ON u.id = up.user_id AND up.is_current = true " + // Join để lấy ảnh hiện tại
            "WHERE r.homestay_id = :homestayId",
            countQuery = "SELECT count(*) FROM reviews WHERE homestay_id = :homestayId",
            nativeQuery = true)
    Page<Map<String, Object>> findReviewsWithUserInfo(Long homestayId, Pageable pageable);

}
