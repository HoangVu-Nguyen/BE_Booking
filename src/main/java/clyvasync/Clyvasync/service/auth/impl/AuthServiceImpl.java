package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.dto.request.*;
import clyvasync.Clyvasync.dto.response.LoginResponse;
import clyvasync.Clyvasync.entity.auth.Role;
import clyvasync.Clyvasync.entity.auth.User;
import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.enums.media.RingtoneType;
import clyvasync.Clyvasync.event.auth.UserRegisteredEvent;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.security.PasswordService;
import clyvasync.Clyvasync.service.auth.AuthService;
import clyvasync.Clyvasync.service.auth.RoleService;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.media.RingtoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PasswordService passwordService;
    private final RoleService roleService;
   private final ApplicationEventPublisher eventPublisher;
   private final UserService userService;

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
    @Transactional
    public void register(RegisterRequest request) {
        log.info("Bắt đầu đăng ký cho email: {}", request.getEmail());
        validateRegisterRequest(request);
        Optional<User> userOptional = userService.findOptionalByEmail(request.getEmail());
        User userToSave;

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            if (existingUser.isActive()) {
                throw new AppException(ResultCode.USER_EXISTED);
            }
            userToSave = updateUserFromRequest(existingUser, request);
        } else {
            userToSave = createUserFromRequest(request);
        }

        assignDefaultResources(userToSave);

        User savedUser = userService.save(userToSave);

        // 5. TUYỆT CHIÊU EVENT: Phát loa báo cho toàn hệ thống biết!
        // Các module khác (như Media) nghe được sẽ tự động gán Ringtone
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));

        log.info("Đăng ký thành công User có email: {}", request.getEmail());

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
    private void assignDefaultResources(User user) {
        Set<Role> roles = Optional.ofNullable(user.getRoles()).orElseGet(HashSet::new);

        if (roles.isEmpty()) {
            roles.add(roleService.getRoleByName(RoleName.USER));
        }
        user.setRoles(roles);

    }

    private User updateUserFromRequest(User user, RegisterRequest request) {
        user.setFullName(request.getUsername());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private User createUserFromRequest(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFullName(request.getUsername());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());
        user.setPasswordHash(passwordService.hashPassword(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(false);
        return user;
    }
    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ResultCode.PASSWORD_NOT_MATCH);
        }
        if (!passwordService.isStrongPassword(request.getPassword())) {
            throw new AppException(ResultCode.PASSWORD_TOO_WEAK);
        }
    }
}
