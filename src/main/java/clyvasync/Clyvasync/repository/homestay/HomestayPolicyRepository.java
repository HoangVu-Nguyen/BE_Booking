package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomestayPolicyRepository extends JpaRepository<HomestayPolicy,Long> {
    Optional<HomestayPolicy> findByHomestayId(Long homestayId);
}
