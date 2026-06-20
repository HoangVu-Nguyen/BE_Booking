package clyvasync.Clyvasync.service.room;

import clyvasync.Clyvasync.dto.request.UpdateRatePlanBenefitsRequest;
import clyvasync.Clyvasync.dto.response.RatePlanBenefitResponse;

import java.util.List;
import java.util.Map;

public interface RatePlanBenefitMappingService {
    Map<Long, List<RatePlanBenefitResponse>> findBenefitsByPlanIds(List<Long> planIds);
    List<RatePlanBenefitResponse> getRatePlanBenefits(
            Long ownerId,
            Long homestayId,
            Long roomId,
            Long ratePlanId
    );

    void updateRatePlanBenefits(
            Long ownerId,
            Long homestayId,
            Long roomId,
            Long ratePlanId,
            UpdateRatePlanBenefitsRequest request
    );
}
