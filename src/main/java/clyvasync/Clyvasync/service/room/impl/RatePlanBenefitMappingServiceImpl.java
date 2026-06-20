package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.dto.request.RatePlanBenefitRequest;
import clyvasync.Clyvasync.dto.request.UpdateRatePlanBenefitsRequest;
import clyvasync.Clyvasync.dto.response.HomestayResponse;
import clyvasync.Clyvasync.dto.response.RatePlanBenefitResponse;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.homestay.entity.Amenity;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RatePlanBenefitMapping;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.homestay.AmenityRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRoomRepository;
import clyvasync.Clyvasync.repository.room.RatePlanBenefitMappingRepository;
import clyvasync.Clyvasync.repository.room.RoomRatePlanRepository;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.room.RatePlanBenefitMappingService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RatePlanBenefitMappingServiceImpl implements RatePlanBenefitMappingService {
    private final RatePlanBenefitMappingRepository ratePlanBenefitMappingRepository;
    private final AmenityRepository amenityRepository;

private final RoomRatePlanService roomRatePlanService;
    @Override
    public Map<Long, List<RatePlanBenefitResponse>> findBenefitsByPlanIds(List<Long> planIds) {
        if (planIds == null || planIds.isEmpty()) return Map.of();

        List<RatePlanBenefitMapping> mappings =
                ratePlanBenefitMappingRepository.findAllByRatePlanIdIn(planIds);

        if (mappings.isEmpty()) return Map.of();

        List<Long> amenityIds = mappings.stream()
                .map(RatePlanBenefitMapping::getAmenityId)
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .distinct()
                .toList();

        Map<Integer, Amenity> amenityMap = amenityRepository.findAllById(amenityIds)
                .stream()
                .collect(Collectors.toMap(
                        Amenity::getId,
                        amenity -> amenity
                ));

        return mappings.stream()
                .collect(Collectors.groupingBy(
                        RatePlanBenefitMapping::getRatePlanId,
                        Collectors.mapping(mapping -> {
                            Amenity amenity = amenityMap.get(mapping.getAmenityId());

                            String displayValue = mapping.getDisplayValue();

                            if (displayValue == null || displayValue.isBlank()) {
                                displayValue = amenity != null ? amenity.getName() : "";
                            }

                            return RatePlanBenefitResponse.builder()
                                    .ratePlanId(mapping.getRatePlanId())
                                    .amenityId(mapping.getAmenityId())
                                    .name(amenity != null ? amenity.getName() : "")
                                    .iconName(amenity != null ? amenity.getIconName() : "")
                                    .groupName(amenity != null ? amenity.getGroupName() : "")
                                    .displayValue(displayValue)
                                    .build();
                        }, Collectors.toList())
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RatePlanBenefitResponse> getRatePlanBenefits(
            Long ownerId,
            Long homestayId,
            Long roomId,
            Long ratePlanId
    ) {
        roomRatePlanService.validateRoomAndRatePlan(ownerId, homestayId, roomId, ratePlanId);

        List<RatePlanBenefitMapping> mappings =
                ratePlanBenefitMappingRepository.findByRatePlanId(ratePlanId);

        if (mappings.isEmpty()) {
            return List.of();
        }

        Set<Integer> amenityIds = mappings.stream()
                .map(RatePlanBenefitMapping::getAmenityId)
                .collect(Collectors.toSet());

        Map<Integer, Amenity> amenityMap = amenityRepository.findAllByIdIn(amenityIds)
                .stream()
                .collect(Collectors.toMap(Amenity::getId, Function.identity()));

        return mappings.stream()
                .map(mapping -> {
                    Amenity amenity = amenityMap.get(mapping.getAmenityId());

                    if (amenity == null) {
                        return null;
                    }

                    return RatePlanBenefitResponse.builder()
                            .ratePlanId(mapping.getRatePlanId())
                            .amenityId(amenity.getId())
                            .name(amenity.getName())
                            .iconName(amenity.getIconName())
                            .groupName(amenity.getGroupName())
                            .displayValue(mapping.getDisplayValue())
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional
    public void updateRatePlanBenefits(
            Long ownerId,
            Long homestayId,
            Long roomId,
            Long ratePlanId,
            UpdateRatePlanBenefitsRequest request
    ) {
            roomRatePlanService.validateRoomAndRatePlan(ownerId, homestayId, roomId, ratePlanId);

        List<RatePlanBenefitRequest> benefits =
                request.getBenefits() == null
                        ? List.of()
                        : request.getBenefits();

        Map<Integer, String> displayValueByAmenityId = new LinkedHashMap<>();

        for (RatePlanBenefitRequest item : benefits) {
            if (item.getAmenityId() == null) {
                continue;
            }

            displayValueByAmenityId.put(
                    item.getAmenityId(),
                    normalizeDisplayValue(item.getDisplayValue())
            );
        }

        Set<Integer> amenityIds = displayValueByAmenityId.keySet();

        validateAmenityIds(amenityIds);

        ratePlanBenefitMappingRepository.deleteAllByRatePlanId(ratePlanId);
        ratePlanBenefitMappingRepository.flush();

        if (amenityIds.isEmpty()) {
            return;
        }

        List<RatePlanBenefitMapping> entities = amenityIds.stream()
                .map(amenityId -> RatePlanBenefitMapping.builder()
                        .ratePlanId(ratePlanId)
                        .amenityId(amenityId)
                        .displayValue(displayValueByAmenityId.get(amenityId))
                        .build())
                .toList();

        ratePlanBenefitMappingRepository.saveAll(entities);
    }



    private void validateAmenityIds(Set<Integer> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return;
        }

        List<Amenity> amenities = amenityRepository.findAllByIdIn(amenityIds);

        if (amenities.size() != amenityIds.size()) {
            throw new AppException(ResultCode.FIELD_REQUIRED);
        }
    }

    private String normalizeDisplayValue(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        if (trimmed.isBlank()) {
            return null;
        }

        if (trimmed.length() > 100) {
            return trimmed.substring(0, 100);
        }

        return trimmed;
    }
}
