package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.config.RabbitMQConfig;
import clyvasync.Clyvasync.constant.MessagingConstants;
import clyvasync.Clyvasync.dto.event.UserEventDTO;
import clyvasync.Clyvasync.dto.request.*;
import clyvasync.Clyvasync.dto.response.LoginResponse;
import clyvasync.Clyvasync.entity.auth.Role;
import clyvasync.Clyvasync.entity.auth.User;
import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.enums.cache.RedisKeyType;
import clyvasync.Clyvasync.enums.media.RingtoneType;
import clyvasync.Clyvasync.event.auth.UserRegisteredEvent;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.producer.AuthProducer;
import clyvasync.Clyvasync.security.PasswordService;
import clyvasync.Clyvasync.service.auth.AuthService;
import clyvasync.Clyvasync.service.auth.RoleService;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.cache.CacheService;
import clyvasync.Clyvasync.service.media.RingtoneService;
import clyvasync.Clyvasync.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

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
    private final AuthProducer authProducer;
    private final CacheService cacheService;
    private final OtpService otpService;

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
        if (cacheService.isSpamming(request.getEmail(), RedisKeyType.SEND_EMAIL_LIMIT)) {
            log.warn("Người dùng {} gửi yêu cầu quá nhanh!", request.getEmail());
            throw new AppException(ResultCode.PLEASE_WAIT_BEFORE_RESENDING);
        }

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
        String otp = otpService.generateOtp();
        cacheService.saveOtp(savedUser.getEmail(), otp, RedisKeyType.VERIFY_ACCOUNT);
        cacheService.setProcessLimit(request.getEmail(), RedisKeyType.SEND_EMAIL_LIMIT);

        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));
        UserEventDTO eventPayload = new UserEventDTO(savedUser.getEmail(), savedUser.getFullName(),otp);

        authProducer.sendRegisterEvent(eventPayload);


        log.info("Đăng ký thành công User có email: {}", request.getEmail());

    }

    @Override
    @Transactional
    public void verifyAccount(VerifyAccountRequest request) {
        String email = request.getEmail();

        User user = userService.findOptionalByEmail(email)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));


        if (user.isActive()) {
            cacheService.delete(RedisKeyType.VERIFY_ACCOUNT.getFullKey(email));
            throw new AppException(ResultCode.USER_ALREADY_ACTIVE);
        }

        String storedToken = cacheService.getOtp(email, RedisKeyType.VERIFY_ACCOUNT);
        if (ObjectUtils.isEmpty(storedToken)) {
            throw new AppException(ResultCode.OTP_EXPIRED);
        }

        if (!storedToken.equals(request.getVerificationCode())) {
            throw new AppException(ResultCode.OTP_INVALID);
        }

        user.setActive(true);
        userService.save(user);

        cacheService.delete(RedisKeyType.VERIFY_ACCOUNT.getFullKey(email));
        cacheService.delete(RedisKeyType.USER_PROFILE.getFullKey(email));

        log.info("Xác thực tài khoản thành công cho email: {}", email);
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
