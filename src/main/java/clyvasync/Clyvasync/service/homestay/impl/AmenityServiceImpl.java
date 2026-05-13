package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.response.AmenityResponse;
import clyvasync.Clyvasync.mapper.homestay.AmenityMapper;
import clyvasync.Clyvasync.modules.homestay.entity.Amenity;
import clyvasync.Clyvasync.repository.homestay.AmenityRepository;
import clyvasync.Clyvasync.repository.projection.AmenityBatchProjection;
import clyvasync.Clyvasync.service.homestay.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AmenityServiceImpl implements AmenityService {
    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;
    @Override
    @Cacheable(value = "homestay_amenities", key = "#homestayId")
    public List<AmenityResponse> getAmenitiesByHomestayId(Long homestayId) {
        return amenityMapper.toAmenityResponseList(amenityRepository.findAllByHomestayId(homestayId));
    }

    @Override
    public Map<Long, List<AmenityResponse>> getAmenitiesForHomestays(List<Long> homestayIds) {
        if (homestayIds == null || homestayIds.isEmpty()) return Map.of();

        List<AmenityBatchProjection> rawData = amenityRepository.findAmenitiesByBatch(homestayIds);

        return rawData.stream().collect(Collectors.groupingBy(
                AmenityBatchProjection::getHomestayId,
                Collectors.mapping(
                        row -> amenityMapper.toAmenityResponse(row.getAmenity()),
                        Collectors.toList()
                )
        ));
    }
}
