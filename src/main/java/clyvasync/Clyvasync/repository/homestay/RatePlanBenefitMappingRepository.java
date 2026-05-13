package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.RatePlanBenefitMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatePlanBenefitMappingRepository extends JpaRepository<RatePlanBenefitMapping, Long> {
    List<RatePlanBenefitMapping> findAllByRatePlanIdIn(List<Long> planIds);
}