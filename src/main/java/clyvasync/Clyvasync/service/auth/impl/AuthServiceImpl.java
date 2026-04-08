package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.dto.event.UserEventDTO;
import clyvasync.Clyvasync.dto.request.*;
import clyvasync.Clyvasync.dto.response.TokenResponse;

import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.enums.cache.RedisKeyType;
import clyvasync.Clyvasync.enums.otp.OtpType;
import clyvasync.Clyvasync.event.auth.UserRegisteredEvent;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.Role;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.producer.AuthProducer;
import clyvasync.Clyvasync.security.PasswordService;
import clyvasync.Clyvasync.security.util.JwtUtil;
import clyvasync.Clyvasync.service.auth.AuthService;
import clyvasync.Clyvasync.service.auth.RefreshTokenService;
import clyvasync.Clyvasync.service.auth.RoleService;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.cache.CacheService;
import clyvasync.Clyvasync.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
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
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final JwtDecoder jwtDecoder;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public TokenResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = request.getEmail().trim().toLowerCase();

        if (cacheService.isAccountLocked(email)) {
            log.warn("Truy cập bị chặn: Tài khoản {} đang bị khóa", email);
            throw new AppException(ResultCode.ACCOUNT_TEMPORARILY_LOCKED);
        }

        // 2. KIỂM TRA BRUTE FORCE (YÊU CẦU CAPTCHA)
        // Nếu sai từ 5-9 lần, bắt buộc phải có Captcha hợp lệ mới được đi tiếp.
        if (cacheService.isBruteForce(email)) {
            if (!StringUtils.hasText(request.getRecaptcha())) {
                throw new AppException(ResultCode.CAPTCHA_REQUIRED);
            }
            // verifyCaptcha(request.getRecaptcha());
        }

        // 3. TÌM USER VÀ KIỂM TRA PASSWORD
        User user = userService.findOptionalByEmail(email)
                .orElseThrow(() -> {
                    // Email không tồn tại cũng phải tăng đếm để kích hoạt Khóa nếu phá hoại
                    cacheService.increaseFailedAttempts(email);
                    return new AppException(ResultCode.LOGIN_FAILED);
                });

        if (!passwordService.matches(request.getPassword(), user.getPasswordHash())) {

            cacheService.increaseFailedAttempts(email);
            throw new AppException(ResultCode.LOGIN_FAILED);
        }

        if (!user.isActive()) {
            throw new AppException(ResultCode.USER_NOT_ACTIVE);
        }

        cacheService.resetFailedAttempts(email);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles(), user.getId());

        String refreshToken = refreshTokenService.issueRefreshToken(
                user.getEmail(),
                request.getDeviceId(),
                userAgent,
                ipAddress
        );

        log.info("User {} login thành công. Device: {}, IP: {}", email, request.getDeviceId(), ipAddress);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void logout(String accessToken, String deviceId) {
        // Trong OAuth2, Token đã được Filter verify trước khi vào tới đây
        // nên bạn không cần gọi jwtUtil.validateToken nữa.

        // 1. Giải mã token thủ công hoặc truyền Instant từ Controller xuống
        // Ở đây tôi giả sử bạn truyền String accessToken xuống:
        Jwt jwt = jwtDecoder.decode(accessToken);
        Instant expiry = jwt.getExpiresAt();
        String email = jwt.getClaimAsString("email");

        if (expiry != null) {
            long remainingTime = expiry.getEpochSecond() - Instant.now().getEpochSecond();
            if (remainingTime > 0) {
                // Đưa vào Redis Blacklist
                cacheService.save("blacklist:" + accessToken, "logout", Duration.ofSeconds(remainingTime));
            }
        }

        // 2. Xóa Refresh Token trong DB
        refreshTokenService.deleteByDeviceIdAndEmail(deviceId, email);
    }

    @Override
    public void logoutAll(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new AppException(ResultCode.INVALID_TOKEN);
        }
        String email = jwtUtil.extractEmail(token);
        refreshTokenService.revokeAllUserSessions(email);

    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Bắt đầu đăng ký cho email: {}", email);

        // 1. Kiểm tra request hợp lệ (Pass mạnh, khớp confirm pass...)
        validateRegisterRequest(request);

        // 2. Chống Spam (Đưa lên đầu để Fail-fast, chặn luôn không cần gọi DB)
        if (cacheService.isSpamming(email, RedisKeyType.SEND_EMAIL_LIMIT)) {
            log.warn("Người dùng {} gửi yêu cầu đăng ký quá nhanh!", email);
            throw new AppException(ResultCode.PLEASE_WAIT_BEFORE_RESENDING);
        }

        // 3. Xử lý logic User
        User userToSave;
        Optional<User> userOptional = userService.findOptionalByEmail(email);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            if (existingUser.isActive()) {
                throw new AppException(ResultCode.USER_EXISTED); // Đã active thì cút
            }
            // Tồn tại nhưng chưa Active (chưa nhập OTP) -> Ghi đè thông tin & Mật khẩu mới
            userToSave = updateUserFromRequest(existingUser, request);
        } else {
            // User hoàn toàn mới
            userToSave = createUserFromRequest(request);
        }

        // Gán Role mặc định (USER)
        assignDefaultResources(userToSave);

        // 4. Lưu xuống Database
        User savedUser = userService.save(userToSave);

        // 5. Tạo OTP, lưu Cache và Set Process Limit (khóa mõm chống gửi tiếp)
        String otp = otpService.generateOtp();
        cacheService.saveOtp(savedUser.getEmail(), otp, RedisKeyType.VERIFY_ACCOUNT);
        cacheService.setProcessLimit(email, RedisKeyType.SEND_EMAIL_LIMIT);

        // 6. Bắn Event nội bộ & Ném vào RabbitMQ cho Worker gửi mail
        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));
        UserEventDTO eventPayload = new UserEventDTO(savedUser.getEmail(), savedUser.getFullName(), otp,OtpType.ACTIVATION.name());
        authProducer.sendRegisterEvent(eventPayload);

        log.info("Đăng ký thành công (Chờ xác thực OTP) cho email: {}", email);
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

        if (!storedToken.equals(request.getCode())) {
            throw new AppException(ResultCode.OTP_INVALID);
        }

        user.setActive(true);
        userService.save(user);

        cacheService.delete(RedisKeyType.VERIFY_ACCOUNT.getFullKey(email));
        cacheService.delete(RedisKeyType.USER_PROFILE.getFullKey(email));

        log.info("Xác thực tài khoản thành công cho email: {}", email);
    }
    @Override
    @Transactional
    public void resendVerification(ResendVerificationRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        OtpType type = request.getType(); // Lấy Enum

        if (cacheService.isSpamming(email, RedisKeyType.SEND_EMAIL_LIMIT)) {
            throw new AppException(ResultCode.PLEASE_WAIT_BEFORE_RESENDING);
        }

        User user = userService.findOptionalByEmail(email)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        if (type == OtpType.ACTIVATION) {
            if (user.isActive()) throw new AppException(ResultCode.USER_ALREADY_ACTIVE);
        } else if (type == OtpType.RECOVERY) {
            if (!user.isActive()) throw new AppException(ResultCode.USER_NOT_ACTIVE);
        }

        String otp = otpService.generateOtp();
        cacheService.saveOtp(email, otp, RedisKeyType.VERIFY_ACCOUNT);
        cacheService.setProcessLimit(email, RedisKeyType.SEND_EMAIL_LIMIT);


        UserEventDTO eventPayload = new UserEventDTO(user.getEmail(), user.getFullName(), otp, type.name());
        authProducer.sendRegisterEvent(eventPayload);

        log.info("Đã gửi lại OTP (Type: {}) thành công cho: {}", type.name(), email);
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        String cleanEmail = email.trim().toLowerCase();

        // 1. Kiểm tra User tồn tại
        User user = userService.findOptionalByEmail(cleanEmail)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        // 2. Chống spam gửi mail
        if (cacheService.isSpamming(cleanEmail, RedisKeyType.SEND_EMAIL_LIMIT)) {
            throw new AppException(ResultCode.PLEASE_WAIT_BEFORE_RESENDING);
        }

        // 3. Tạo mã OTP khôi phục (Dùng chung Type VERIFY_ACCOUNT hoặc tạo Type mới FORGOT_PASSWORD)
        String otp = otpService.generateOtp();
        cacheService.saveOtp(cleanEmail, otp, RedisKeyType.VERIFY_ACCOUNT);
        cacheService.setProcessLimit(cleanEmail, RedisKeyType.SEND_EMAIL_LIMIT);

        // 4. Bắn vào RabbitMQ để gửi mail "Khôi phục mật khẩu"
        // Gợi ý: Bạn có thể tạo một Event riêng hoặc dùng chung RegisterEvent với template mail khác
        UserEventDTO eventPayload = new UserEventDTO(user.getEmail(), user.getFullName(), otp,OtpType.RECOVERY.name());
        authProducer.sendRegisterEvent(eventPayload);

        log.info("Yêu cầu khôi phục mật khẩu cho email: {}", cleanEmail);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        return "";
    }

    @Override
    public void verifyPasswordResetOtp(String otp, String email) {
        String cleanEmail = email.trim().toLowerCase();

        userService.findOptionalByEmail(cleanEmail)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        String storedOtp = cacheService.getOtp(cleanEmail, RedisKeyType.VERIFY_ACCOUNT);
        if (ObjectUtils.isEmpty(storedOtp)) {
            throw new AppException(ResultCode.OTP_EXPIRED);
        }

        if (!storedOtp.equals(otp)) {
            throw new AppException(ResultCode.OTP_INVALID);
        }

        log.info("Xác thực mã OTP khôi phục hợp lệ cho email: {}", cleanEmail);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String newPassword, String otp) {
        String cleanEmail = email.trim().toLowerCase();

        User user = userService.findOptionalByEmail(cleanEmail)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));


        String storedOtp = cacheService.getOtp(cleanEmail, RedisKeyType.VERIFY_ACCOUNT);
        if (ObjectUtils.isEmpty(storedOtp)) {
            throw new AppException(ResultCode.OTP_EXPIRED);
        }
        if (!storedOtp.equals(otp)) {
            throw new AppException(ResultCode.OTP_INVALID);
        }

        if (!passwordService.isStrongPassword(newPassword)) {
            throw new AppException(ResultCode.PASSWORD_TOO_WEAK);
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new AppException(ResultCode.PASSWORD_NOT_MATCH);
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userService.save(user);

        cacheService.delete(RedisKeyType.VERIFY_ACCOUNT.getFullKey(cleanEmail));
        refreshTokenService.revokeAllUserSessions(cleanEmail);

        log.info("STAGE CLEARED: Đã đặt lại mật khẩu thành công cho email: {}", cleanEmail);
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


    private User updateUserFromRequest(User user, RegisterRequest request) {
        user.setFullName(request.getUsername());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());

        // MA THUẬT NẰM Ở ĐÂY: Hàm encode này tự động nối Pepper và băm bằng Argon2
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private User createUserFromRequest(RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setFullName(request.getUsername());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setGender(request.getGender());
        user.setBirthDate(request.getBirthDate());

        // MA THUẬT NẰM Ở ĐÂY
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setActive(false);
        return user;
    }

    private void assignDefaultResources(User user) {
        Set<Role> roles = Optional.ofNullable(user.getRoles()).orElseGet(HashSet::new);
        if (roles.isEmpty()) {
            roles.add(roleService.getRoleByName(RoleName.USER));
        }
        user.setRoles(roles);
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new AppException(ResultCode.PASSWORD_NOT_MATCH);
        }
        if (!passwordService.isStrongPassword(request.getPassword())) {
            throw new AppException(ResultCode.PASSWORD_TOO_WEAK);
        }
    }
    private boolean statusCaptcha(int count) {
        return count >= 3;
    }
}
