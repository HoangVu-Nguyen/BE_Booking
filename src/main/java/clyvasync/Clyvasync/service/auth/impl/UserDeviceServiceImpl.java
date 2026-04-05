package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.dto.response.UserLoginHistoryResponse;
import clyvasync.Clyvasync.entity.auth.UserDevice;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.repository.auth.UserDeviceRepository;
import clyvasync.Clyvasync.security.util.JwtUtil;
import clyvasync.Clyvasync.service.auth.RefreshTokenService;
import clyvasync.Clyvasync.service.auth.UserDeviceService;
import clyvasync.Clyvasync.util.IPAddressUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeviceServiceImpl implements UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional(readOnly = true)
    public List<UserLoginHistoryResponse> getLoginHistory(String authHeader, String currentRefreshToken) {
        Long userId = extractUserIdFromHeader(authHeader);

        // Fetch danh sách device kèm token (nhờ EntityGraph trong Repo)
        List<UserDevice> devices = userDeviceRepository.findByUserIdOrderByLastActiveDesc(userId);

        return devices.stream().map(device -> {
            // Kiểm tra session hiện tại an toàn hơn
            boolean isCurrent = currentRefreshToken != null &&
                    device.getRefreshToken() != null &&
                    currentRefreshToken.equals(device.getRefreshToken().getToken());

            return UserLoginHistoryResponse.builder()
                    .id(device.getId())
                    .deviceName(IPAddressUtil.formatDeviceName(device.getDeviceName()))
                    .location(device.getLocation())
                    .lastActive(device.getLastActive())
                    .isCurrentDevice(isCurrent)
                    .iconType(IPAddressUtil.determineIconType(device.getDeviceName(), device.getDeviceType()))
                    .ipAddress(device.getIpAddress())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeDevice(Long deviceId, String authHeader) {
        Long currentUserId = extractUserIdFromHeader(authHeader);

        UserDevice device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new AppException(ResultCode.DEVICE_NOT_FOUND));

        // SECURITY: Chỉ chủ sở hữu mới được xóa thiết bị
        if (!device.getUserId().equals(currentUserId)) {
            log.error("Cảnh báo: User {} cố gắng xóa thiết bị {} của User {}",
                    currentUserId, deviceId, device.getUserId());
            throw new AppException(ResultCode.ACCESS_DENIED);
        }

        // Nếu thiết bị có RefreshToken, thu hồi nó trước (Logic quan trọng)
        if (device.getRefreshToken() != null) {
            refreshTokenService.revokeToken(device.getRefreshToken().getToken());
        }

        userDeviceRepository.delete(device);
        log.info("Đã đăng xuất thiết bị ID: {} cho người dùng {}", deviceId, currentUserId);
    }

    @Override
    @Transactional
    public void deleteByRefreshToken(String token) {
        userDeviceRepository.findByRefreshToken_Token(token)
                .ifPresent(device -> {
                    userDeviceRepository.delete(device);
                    log.debug("Đã xóa thông tin thiết bị liên kết với token: {}", token);
                });
    }

    /**
     * Tận dụng logic đã có trong JwtUtil để trích xuất ID
     */
    private Long extractUserIdFromHeader(String authHeader) {
        try {
            // Sử dụng hàm resolveToken hoặc xử lý trực tiếp
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.extractUserId(token);
            if (userId == null) throw new Exception();
            return userId;
        } catch (Exception e) {
            log.error("Lỗi trích xuất UserID từ Header: {}", e.getMessage());
            throw new AppException(ResultCode.PERMISSION_DENIED);
        }
    }
}