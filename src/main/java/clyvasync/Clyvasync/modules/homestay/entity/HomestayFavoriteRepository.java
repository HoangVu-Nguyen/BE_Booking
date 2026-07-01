package clyvasync.Clyvasync.modules.homestay.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface HomestayFavoriteRepository extends JpaRepository<HomestayFavorite, Long> {

    // Lấy danh sách homestay đã lưu của 1 user, mới nhất trước (màn hình "Đã lưu")
    List<HomestayFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Kiểm tra 1 homestay đã được user lưu chưa (để hiện icon tim tô màu hay không)
    boolean existsByUserIdAndHomestayId(Long userId, Long homestayId);

    // Lấy bản ghi cụ thể (dùng khi cần xóa/unfavorite)
    Optional<HomestayFavorite> findByUserIdAndHomestayId(Long userId, Long homestayId);

    // Đếm tổng số lượt yêu thích của 1 homestay (hiển thị "X người đã lưu")
    long countByHomestayId(Long homestayId);

    // Bỏ lưu (unfavorite) - xóa theo user + homestay
    @Transactional
    void deleteByUserIdAndHomestayId(Long userId, Long homestayId);

    // Lấy danh sách ID các homestay mà user đã lưu (dùng để đánh dấu hàng loạt trong list search)
    @Query("SELECT hf.homestayId FROM HomestayFavorite hf WHERE hf.userId = :userId")
    List<Long> findHomestayIdsByUserId(@Param("userId") Long userId);
}