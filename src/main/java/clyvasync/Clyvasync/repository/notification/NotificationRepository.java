package clyvasync.Clyvasync.repository.notification;

import clyvasync.Clyvasync.enums.type.NotificationType;
import clyvasync.Clyvasync.modules.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    // Tối ưu: Đếm số thông báo chưa đọc bằng Index partial đã tạo ở V24
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // Tối ưu: Lấy danh sách thông báo theo bộ lọc.
    // Dùng @Query để tránh lấy thừa dữ liệu nếu entity quá lớn
    @Query("SELECT n FROM Notification n WHERE n.recipientId = :userId " +
            "AND (:filter IS NULL OR :filter = 'ALL' OR n.type IN :types)")
    Page<Notification> findAllByRecipientIdAndFilter(
            @Param("userId") Long userId,
            @Param("types") List<NotificationType> types,
            @Param("filter") String filter,
            Pageable pageable
    );

    // Update hàng loạt cực nhanh
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(Long userId);
}
