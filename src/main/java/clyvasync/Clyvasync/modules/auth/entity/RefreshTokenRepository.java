package clyvasync.Clyvasync.modules.auth.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByEmailAndRevokedFalse(String email);

    Optional<RefreshToken> findByEmailAndDeviceIdAndRevokedFalse(String email, String deviceId);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.updatedAt = CURRENT_TIMESTAMP WHERE r.email = :email")
    int revokeAllByEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.updatedAt = CURRENT_TIMESTAMP WHERE r.email = :email AND r.deviceId = :deviceId")
    int revokeByEmailAndDeviceId(@Param("email") String email, @Param("deviceId") String deviceId);

    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < :now")
    int deleteExpiredTokens(@Param("now") Instant now);

    long countByEmailAndRevokedFalse(String email);
}