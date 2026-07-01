package clyvasync.Clyvasync.modules.homestay.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Lấy tất cả review của 1 homestay, mới nhất trước
    List<Review> findByHomestayIdOrderByCreatedAtDesc(Long homestayId);

    // Tính điểm rating trung bình của 1 homestay (dùng để cập nhật lại Homestay.averageRating)
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.homestayId = :homestayId")
    Double calculateAverageRating(@Param("homestayId") Long homestayId);

    // Đếm tổng số review của 1 homestay
    long countByHomestayId(Long homestayId);

    // Kiểm tra 1 booking đã được review chưa (tránh review trùng cho cùng booking)
    Optional<Review> findByBookingId(Long bookingId);

    // Lấy các review chưa được host phản hồi (phục vụ màn hình "Cần trả lời")
    @Query("SELECT r FROM Review r WHERE r.homestayId = :homestayId AND r.replyFromHost IS NULL")
    List<Review> findUnansweredReviewsByHomestayId(@Param("homestayId") Long homestayId);
}