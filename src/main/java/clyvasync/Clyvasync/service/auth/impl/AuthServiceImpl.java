package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.dto.request.*;
import clyvasync.Clyvasync.dto.response.LoginResponse;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.security.PasswordService;
import clyvasync.Clyvasync.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PasswordService passwordService;

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        return null;
    }

    @Override
    public void logout(String accessToken, String deviceId) {

    }

    @Override
    public void logoutAll(String token) {

    }

    @Override
    public void register(RegisterRequest request) {
        log.info("Bắt đầu đăng ký cho email: {}", request.getEmail());
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ResultCode.PASSWORD_NOT_MATCH);
        }
        if (!passwordService.isStrongPassword(request.getPassword())) {
            throw new AppException(ResultCode.PASSWORD_TOO_WEAK);
        }

    }

    @Override
    public void verifyAccount(VerifyAccountRequest request) {

    }

    @Override
    public void resendVerification(ResendVerificationRequest request) {

    }

    @Override
    public void forgotPassword(String email) {

    }

    @Override
    public String validatePasswordResetToken(String token) {
        return "";
    }

    @Override
    public void verifyPasswordResetOtp(String otp, String email) {

    }

    @Override
    public void resetPassword(String email, String newPassword, String otp) {

    }

    @Override
    public void validateCode(String code, String email) {

    }

    @Override
    public void changePassword(ChangePasswordRequest request, String authHeader, String refreshTokenCookie) {

    }

    @Override
    public boolean isCaptchaRequired(int failedAttempts) {
        return false;
    }
}
