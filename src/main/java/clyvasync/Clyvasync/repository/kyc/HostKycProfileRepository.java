package clyvasync.Clyvasync.repository.kyc;

import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HostKycProfileRepository extends JpaRepository<HostKycProfile, Long> {
    Optional<HostKycProfile> findByUserId(Long userId);
    @Query("SELECT p.id FROM HostKycProfile p WHERE p.userId = :userId")
    Optional<Long> findIdByUserId(@Param("userId") Long userId);
    List<HostKycProfile> findByStatus(KycProfileStatus status);
    long countByStatus(KycProfileStatus status);

}
