package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HomestayPolicyRepository extends JpaRepository<HomestayPolicy,Long> {
    Optional<HomestayPolicy> findByHomestayId(Long homestayId);
    List<HomestayPolicy> findAllByHomestayId(Long homestayId);

}
