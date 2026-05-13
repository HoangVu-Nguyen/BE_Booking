package clyvasync.Clyvasync.service.room;

import java.util.List;
import java.util.Map;

public interface RatePlanBenefitMappingService {
    Map<Long, List<String>> findBenefitsByPlanIds(List<Long> planIds);
}
