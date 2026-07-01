package clyvasync.Clyvasync.controller.admin;

import clyvasync.Clyvasync.dto.response.AdminUserListResponse;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ApiResponse<AdminUserListResponse> getAdminUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "ALL") String role,
            @RequestParam(required = false, defaultValue = "ALL") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(userService.getAdminUsers(keyword, role, status, page, size));
    }

    @PutMapping("/{id}/toggle-status")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Long id) {
        userService.toggleUserStatus(id);
        return ApiResponse.success(null);
    }
}
