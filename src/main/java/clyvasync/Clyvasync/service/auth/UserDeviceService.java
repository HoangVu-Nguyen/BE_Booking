package clyvasync.Clyvasync.service.auth;


import clyvasync.Clyvasync.dto.response.UserLoginHistoryResponse;
import java.util.List;

public interface UserDeviceService {

    /**
     * Lấy danh sách lịch sử đăng nhập của người dùng
     */
    List<UserLoginHistoryResponse> getLoginHistory(String accessToken, String currentRefreshToken);

    /**
     * Đăng xuất từ xa một thiết bị cụ thể theo ID
     */
    void revokeDevice(Long deviceId, String currentAccessToken);

    /**
     * Xóa thông tin thiết bị theo RefreshToken (Dùng nội bộ khi logout/token expired)
     */
    void deleteByRefreshToken(String token);
}