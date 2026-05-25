package clyvasync.Clyvasync.controller.notification;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.NotificationResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.NotificationResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ApiResponse<Page<NotificationResponse>> getNotifications(
            @RequestParam(required = false, defaultValue = "ALL") String filter,
            @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @CurrentUserId Long userId) {

        return ApiResponse.success(notificationService.getUserNotifications(userId, filter, pageable));
    }

    // 2. Đếm số thông báo chưa đọc
    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(@CurrentUserId Long userId) {
        return ApiResponse.success(notificationService.countUnreadNotifications(userId));
    }

    // 3. Đánh dấu một thông báo là đã đọc
    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id, @CurrentUserId Long userId) {
        notificationService.markAsRead(id, userId);
        return ApiResponse.success(null);
    }

    // 4. Đánh dấu tất cả là đã đọc
    @PatchMapping("/read-all")
    public ApiResponse<Void> markAllAsRead(@CurrentUserId Long userId) {
        notificationService.markAllAsRead(userId);
        return ApiResponse.success(null);
    }
}