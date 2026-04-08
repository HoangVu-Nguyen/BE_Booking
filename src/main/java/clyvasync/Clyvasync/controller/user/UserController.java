package clyvasync.Clyvasync.controller.user;


import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.UserHeaderResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.auth.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/header")
    public ApiResponse<UserHeaderResponse> getHeaderInfo(@CurrentUserId Long userId) {
        return ApiResponse.success(userService.getHeaderInfo(userId));
    }
}