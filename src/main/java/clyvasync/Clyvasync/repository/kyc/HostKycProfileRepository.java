package clyvasync.Clyvasync.repository.kyc;

import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HostKycProfileRepository extends JpaRepository<HostKycProfile, Long> {
    Optional<HostKycProfile> findByUserId(Long userId);

}
