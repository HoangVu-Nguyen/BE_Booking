package clyvasync.Clyvasync.repository.auth;

import clyvasync.Clyvasync.entity.auth.RefreshToken;
import clyvasync.Clyvasync.entity.auth.UserDevice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {

    // Sử dụng EntityGraph để lấy luôn RefreshToken trong 1 lần SELECT (Tối ưu hiệu năng)
    @EntityGraph(attributePaths = {"refreshToken"})
    List<UserDevice> findByUserIdOrderByLastActiveDesc(Long userId);

    Optional<UserDevice> findByRefreshToken(RefreshToken refreshToken);

    // Tìm Device dựa trên chuỗi Token (Dùng cho logic xóa session)
    Optional<UserDevice> findByRefreshToken_Token(String token);

    @Modifying
    void deleteByRefreshToken(RefreshToken refreshToken);

    @Modifying
    void deleteByUserId(Long userId);
}