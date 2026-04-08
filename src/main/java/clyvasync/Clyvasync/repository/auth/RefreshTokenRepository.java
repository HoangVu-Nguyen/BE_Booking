package clyvasync.Clyvasync.repository.auth;

import clyvasync.Clyvasync.modules.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken rf WHERE rf.email = :email")
    void deleteAllByEmail(@Param("email") String email);
    boolean existsByToken(String token);
    List<RefreshToken> findByDeviceId(String deviceID);
    void delete(RefreshToken refreshToken);
    boolean existsRefreshTokenByEmailAndDeviceId(String email, String deviceId);
    Optional<RefreshToken> findByDeviceIdAndEmail(String deviceId,String email);
    @Modifying // Bắt buộc vì đây là lệnh DELETE/UPDATE
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.email = :email AND r.token != :currentToken")
    void deleteAllByEmailAndTokenNot(@Param("email") String  email, @Param("currentToken") String currentToken);
    // Xóa session cũ trên cùng 1 thiết bị khi login mới
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.email = :email AND r.deviceId = :deviceId")
    void deleteByEmailAndDeviceId(@Param("email") String email, @Param("deviceId") String deviceId);
    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.deviceId = :deviceId AND r.email = :email")
    void deleteByDeviceIdAndEmail(@Param("deviceId") String deviceId, @Param("email") String email);

}