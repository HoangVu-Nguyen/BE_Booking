package clyvasync.Clyvasync.controller.auth;

import clyvasync.Clyvasync.dto.request.LoginRequest;
import clyvasync.Clyvasync.dto.request.RegisterRequest;
import clyvasync.Clyvasync.dto.request.VerifyAccountRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Các API liên quan đến Xác thực (Đăng ký, Đăng nhập, Quên mật khẩu)")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Đăng ký tài khoản mới", description = "Tạo user mới, gán role mặc định và phát sự kiện tạo nhạc chuông.")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST Request to register user with email: {}", request.getEmail());
        authService.register(request);
        return ApiResponse.success(ResultCode.REGISTER_SUCCESS);


    }

    // Viết sẵn khung cho Login
    @Operation(summary = "Đăng nhập hệ thống")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("REST Request to login user with email: {}", request.getEmail());

        // TODO: Gọi authService.login(...)
        return ResponseEntity.ok("Login logic here");
    }
    /**
     * Endpoint xác thực tài khoản qua OTP
     */
    @PostMapping("/verify-account")
    public ApiResponse<Void> verifyAccount(
            @RequestBody @Valid VerifyAccountRequest request
    ) {
        log.info("Nhận yêu cầu xác thực tài khoản cho email: {}", request.getEmail());
        authService.verifyAccount(request);
        return ApiResponse.success(ResultCode.ACTIVATION_SUCCESS);
    }
}