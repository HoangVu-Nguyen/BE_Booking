package clyvasync.Clyvasync.service.auth;

import clyvasync.Clyvasync.dto.request.*;
import clyvasync.Clyvasync.dto.response.LoginResponse;
import clyvasync.Clyvasync.dto.response.TokenResponse;

public interface AuthService {
    // =====================================================================
    // 1. AUTHENTICATION (Đăng nhập / Đăng xuất)
    // =====================================================================

    TokenResponse login(LoginRequest request, String ipAddress, String userAgent);

    void logout(String accessToken, String deviceId);

    void logoutAll(String token);


    // =====================================================================
    // 2. REGISTRATION & VERIFICATION (Đăng ký / Xác thực tài khoản)
    // =====================================================================

    void register(RegisterRequest request);

    void verifyAccount(VerifyAccountRequest request);

    void resendVerification(ResendVerificationRequest request);


    // =====================================================================
    // 3. PASSWORD MANAGEMENT (Quản lý mật khẩu)
    // =====================================================================

    void forgotPassword(String email);

    String validatePasswordResetToken(String token);

    void verifyPasswordResetOtp(String otp, String email); // Đổi tham số token -> otp cho rõ nghĩa

    void resetPassword(String email, String newPassword, String otp); // Đổi code -> otp

    void validateCode(String code, String email);

    void changePassword(ChangePasswordRequest request, String authHeader, String refreshTokenCookie);


    // =====================================================================
    // 4. UTILITIES (Tiện ích hỗ trợ)
    // =====================================================================

    /**
     * Kiểm tra xem số lần đăng nhập thất bại đã vượt quá giới hạn để yêu cầu Captcha hay chưa.
     * Tên cũ: statusCaptcha
     */
    boolean isCaptchaRequired(int failedAttempts);
}
