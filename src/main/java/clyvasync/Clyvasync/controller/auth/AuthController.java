package clyvasync.Clyvasync.controller.auth;

import clyvasync.Clyvasync.dto.request.LoginRequest;
import clyvasync.Clyvasync.dto.request.LogoutRequest;
import clyvasync.Clyvasync.dto.request.RegisterRequest;
import clyvasync.Clyvasync.dto.request.VerifyAccountRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.LoginResponse;
import clyvasync.Clyvasync.dto.response.TokenResponse;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.auth.AuthService;
import clyvasync.Clyvasync.utils.Translator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "Các API liên quan đến Xác thực (Đăng ký, Đăng nhập, Quên mật khẩu)")
public class AuthController {

    private final AuthService authService;
    private final Translator translator;
    @Operation(summary = "Đăng ký tài khoản mới")
    @Parameter(
            name = "Accept-Language",
            description = "Ngôn ngữ phản hồi (vi, en, zh)",
            in = ParameterIn.HEADER,
            schema = @Schema(type = "string", defaultValue = "vi")
    )
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(
            @RequestBody @Valid LoginRequest request,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse
    ) {
        log.info("REST Request to login user with email: {}", request.getEmail());

        String ipAddress = httpServletRequest.getRemoteAddr();
        String userAgent = httpServletRequest.getHeader("User-Agent");


        TokenResponse tokenResponse = authService.login(request, ipAddress, userAgent);

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refresh_token", tokenResponse.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();

        httpServletResponse.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken(tokenResponse.getAccessToken())
                .build();

        return ApiResponse.success(loginResponse);
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        log.info("REST Request to register user with email: {}", request.getEmail());

        authService.register(request);

        return ApiResponse.success(
                ResultCode.REGISTER_SUCCESS,
                translator.toLocale(ResultCode.REGISTER_SUCCESS)
        );
    }

    @Operation(summary = "Xác thực tài khoản", description = "Kích hoạt tài khoản người dùng bằng mã OTP.")
    @PostMapping("/verify-account")
    public ApiResponse<Void> verifyAccount(@RequestBody @Valid VerifyAccountRequest request) {
        log.info("Nhận yêu cầu xác thực tài khoản cho email: {}", request.getEmail());

        authService.verifyAccount(request);

        return ApiResponse.success(
                ResultCode.ACTIVATION_SUCCESS,
                translator.toLocale(ResultCode.ACTIVATION_SUCCESS)
        );
    }

    @Operation(summary = "Đăng xuất", description = "Vô hiệu hóa Access Token (đưa vào Blacklist) và xóa Refresh Token của thiết bị.")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid LogoutRequest logoutRequest
    ) {
        String tokenValue = jwt.getTokenValue();
        log.info("Yêu cầu đăng xuất từ user email: {}", jwt.getClaimAsString("email"));

        authService.logout(tokenValue, logoutRequest.getDeviceId());

        return ApiResponse.success(
                ResultCode.LOGOUT_SUCCESS,
                translator.toLocale(ResultCode.LOGOUT_SUCCESS)
        );
    }

}