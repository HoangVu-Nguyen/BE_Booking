package clyvasync.Clyvasync.mapper.notification;

import clyvasync.Clyvasync.dto.response.NotificationResponse;
import clyvasync.Clyvasync.enums.type.NotificationType;
import clyvasync.Clyvasync.modules.notification.entity.Notification;
import org.mapstruct.Mapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
@Mapper(componentModel = "spring")
public interface NotificationMapper {

       static NotificationResponse toResponse(Notification entity) {
        return NotificationResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .category(getCategoryFromType(entity.getType()))
                .title(entity.getTitle())
                .message(entity.getMessage())
                .isRead(entity.isRead())
                .createdAt(entity.getCreatedAt())
                .timeAgo(calculateTimeAgo(entity.getCreatedAt()))
                .metadata(entity.getMetadata())
                .build();
    }

    // Logic tính "10 phút trước", "2 giờ trước" siêu chuẩn
    private static String calculateTimeAgo(OffsetDateTime createdAt) {
        if (createdAt == null) return "Vừa xong";

        Duration duration = Duration.between(createdAt, OffsetDateTime.now());
        long seconds = duration.getSeconds();

        if (seconds < 60) {
            return "Vừa xong";
        } else if (seconds < 3600) {
            return (seconds / 60) + " phút trước";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " giờ trước";
        } else if (seconds < 2592000) { // Dưới 30 ngày
            return (seconds / 86400) + " ngày trước";
        } else if (seconds < 31536000) { // Dưới 1 năm
            return (seconds / 2592000) + " tháng trước";
        } else {
            return (seconds / 31536000) + " năm trước";
        }
    }

    // Phân loại nhóm cho Tab (Dựa vào Enum của bác)
    private static String getCategoryFromType(NotificationType type) {
        String typeStr = type.name();
        if (typeStr.startsWith("BOOKING_")) return "BOOKING";
        if (typeStr.startsWith("PAYMENT_")) return "SYSTEM";
        if (typeStr.equals("SYSTEM_ALERT")) return "SYSTEM";
        return "ALL";
    }
}