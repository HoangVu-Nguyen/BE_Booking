package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RatePlanBenefitMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RatePlanBenefitMappingRepository extends JpaRepository<RatePlanBenefitMapping, Long> {
    List<RatePlanBenefitMapping> findAllByRatePlanIdIn(List<Long> planIds);
}