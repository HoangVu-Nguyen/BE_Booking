package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.dto.request.RefreshTokenRequest;
import clyvasync.Clyvasync.dto.response.TokenResponse;
import clyvasync.Clyvasync.entity.auth.RefreshToken;
import clyvasync.Clyvasync.entity.auth.User;
import clyvasync.Clyvasync.entity.auth.UserDevice;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.repository.auth.RefreshTokenRepository;
import clyvasync.Clyvasync.repository.auth.UserDeviceRepository;
import clyvasync.Clyvasync.security.util.JwtUtil;
import clyvasync.Clyvasync.service.auth.RefreshTokenService;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.util.GeoIPService;
import clyvasync.Clyvasync.util.IPAddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final GeoIPService geoIPService;

    @Override
    @Transactional
    public String issueRefreshToken(String email, String deviceId, String userAgent, String ipAddress) {
        log.info("Cấp Refresh Token mới cho: {} trên thiết bị: {}", email, deviceId);

        // 1. Dọn dẹp session cũ trên cùng thiết bị (Tránh rác DB)
        refreshTokenRepository.deleteByEmailAndDeviceId(email, deviceId);

        // 2. Tạo thực thể RefreshToken (UUID Opaque Token)
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .email(email)
                .expiryDate(Instant.now().plus(30, ChronoUnit.DAYS))
                .deviceId(deviceId)
                .revoked(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        // 3. Cập nhật thông tin chi tiết thiết bị (UserDevice)
        syncUserDevice(savedToken, userAgent, ipAddress);

        return savedToken.getToken();
    }

    @Override
    @Transactional
    public TokenResponse rotateTokens(RefreshTokenRequest request, String ipAddress, String userAgent) {
        // 1. Kiểm tra Token tồn tại và khớp metadata
        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .filter(t -> t.getDeviceId().equals(request.getDeviceId()))
                .filter(t -> t.getEmail().equals(request.getEmail()))
                .orElseThrow(() -> new AppException(ResultCode.INVALID_TOKEN));

        // 2. Kiểm tra hiệu lực (Hết hạn hoặc đã bị Revoke)
        if (oldToken.getExpiryDate().isBefore(Instant.now()) || oldToken.isRevoked()) {
            deleteSessionInternal(oldToken);
            throw new AppException(ResultCode.TOKEN_EXPIRED);
        }

        // 3. Lấy thông tin User
        User user = userService.findOptionalByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        // 4. TOKEN ROTATION: Đổi mã token mới để chống Replay Attack
        String newTokenString = UUID.randomUUID().toString();
        oldToken.setToken(newTokenString);
        oldToken.setExpiryDate(Instant.now().plus(30, ChronoUnit.DAYS));

        refreshTokenRepository.save(oldToken);

        // 5. Đồng bộ metadata thiết bị mới nhất
        syncUserDevice(oldToken, userAgent, ipAddress);

        // 6. Tạo Access Token mới
        String newAccessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRoles(), user.getId());

        log.info("Xoay vòng token thành công cho user: {}", user.getEmail());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newTokenString)
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public void revokeToken(String token) {
        refreshTokenRepository.findByToken(token)
                .ifPresent(this::deleteSessionInternal);
    }

    @Override
    @Transactional
    public void revokeAllUserSessions(String email) {
        log.warn("Force Logout toàn bộ session của: {}", email);
        // DB CASCADE sẽ tự dọn bảng user_devices
        refreshTokenRepository.deleteAllByEmail(email);
    }

    @Override
    @Transactional
    public void revokeOtherSessions(String email, String currentToken) {
        log.info("Thu hồi tất cả session khác của user: {}", email);
        refreshTokenRepository.deleteAllByEmailAndTokenNot(email, currentToken);
    }

    // --- Helpers ---

    private void syncUserDevice(RefreshToken token, String userAgent, String ipAddress) {
        UserDevice device = userDeviceRepository.findByRefreshToken(token)
                .orElse(new UserDevice());

        User user = userService.findOptionalByEmail(token.getEmail())
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        device.setUserId(user.getId());
        device.setRefreshToken(token);
        device.setDeviceName(IPAddressUtil.parseDeviceName(userAgent));
        device.setDeviceType(IPAddressUtil.parseDeviceType(userAgent));
        device.setIpAddress(ipAddress);
        device.setLocation(geoIPService.getLocationFromIp(ipAddress));
        device.setLastActive(LocalDateTime.now());

        userDeviceRepository.save(device);
    }

    private void deleteSessionInternal(RefreshToken token) {
        userDeviceRepository.deleteByRefreshToken(token);
        refreshTokenRepository.delete(token);
    }
}