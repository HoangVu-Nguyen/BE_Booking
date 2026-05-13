package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.modules.room.RatePlanBenefitMapping;
import clyvasync.Clyvasync.repository.homestay.AmenityRepository;
import clyvasync.Clyvasync.repository.room.RatePlanBenefitMappingRepository;
import clyvasync.Clyvasync.service.room.RatePlanBenefitMappingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RatePlanBenefitMappingServiceImpl implements RatePlanBenefitMappingService {
    private final RatePlanBenefitMappingRepository ratePlanBenefitMappingRepository;
    private final AmenityRepository amenityRepository;
    @Override
    public Map<Long, List<String>> findBenefitsByPlanIds(List<Long> planIds) {
        if (planIds.isEmpty()) return Map.of();
        return ratePlanBenefitMappingRepository.findAllByRatePlanIdIn(planIds).stream()
                .collect(Collectors.groupingBy(RatePlanBenefitMapping::getRatePlanId,
                        Collectors.mapping(m -> {
                            var amenity = amenityRepository.findById(m.getAmenityId().longValue()).orElse(null);
                            return amenity != null ? amenity.getName() : "";
                        }, Collectors.toList())));
    }
}
