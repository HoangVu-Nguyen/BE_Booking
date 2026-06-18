package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.request.RoomAmenityHighlightRequest;
import clyvasync.Clyvasync.dto.request.UpdateHomestayAmenitiesRequest;
import clyvasync.Clyvasync.dto.request.UpdateRoomAmenityHighlightsRequest;
import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.dto.response.AmenityResponse;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.homestay.AmenityMapper;
import clyvasync.Clyvasync.modules.homestay.entity.Amenity;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayAmenity;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RoomAmenityHighlight;
import clyvasync.Clyvasync.repository.homestay.AmenityRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayAmenityRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRoomRepository;
import clyvasync.Clyvasync.repository.projection.AmenityBatchProjection;
import clyvasync.Clyvasync.repository.room.RoomAmenityHighlightRepository;
import clyvasync.Clyvasync.service.homestay.AmenityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AmenityServiceImpl implements AmenityService {
    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;
    private final HomestayAmenityRepository homestayAmenityRepository;
    private final HomestayRepository homestayRepository;
    private final RoomAmenityHighlightRepository roomAmenityHighlightRepository;
    private final HomestayRoomRepository homestayRoomRepository;
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

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAllByOrderByGroupNameAscNameAsc()
                .stream()
                .map(this.amenityMapper::toAmenityResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getHomestayAmenityIds(Long homestayId) {
        return homestayAmenityRepository.findByHomestayId(homestayId)
                .stream()
                .map(HomestayAmenity::getAmenityId)
                .toList();
    }

    @Override
    @Transactional
    public void updateHomestayAmenities(
            Long ownerId,
            Long homestayId,
            UpdateHomestayAmenitiesRequest request
    ) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        if (!homestay.getOwnerId().equals(ownerId)) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }

        Set<Integer> amenityIds = request.getAmenityIds() == null
                ? Set.of()
                : new HashSet<>(request.getAmenityIds());

        validateAmenityIds(amenityIds);

        homestayAmenityRepository.deleteByHomestayId(homestayId);

        List<HomestayAmenity> newItems = amenityIds.stream()
                .map(amenityId -> {
                    HomestayAmenity item = new HomestayAmenity();
                    item.setHomestayId(homestayId);
                    item.setAmenityId(amenityId);
                    return item;
                })
                .toList();

        homestayAmenityRepository.saveAll(newItems);
    }


    @Override
    @Transactional(readOnly = true)
    public List<AmenityHighlightResponse> getRoomAmenityHighlights(Long roomId) {
        List<RoomAmenityHighlight> highlights = roomAmenityHighlightRepository.findByRoomId(roomId);

        Set<Integer> amenityIds = highlights.stream()
                .map(RoomAmenityHighlight::getAmenityId)
                .collect(java.util.stream.Collectors.toSet());

        Map<Integer, Amenity> amenityMap = amenityRepository.findAllByIdIn(amenityIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(Amenity::getId, amenity -> amenity));

        return highlights.stream()
                .map(item -> {
                    Amenity amenity = amenityMap.get(item.getAmenityId());

                    return AmenityHighlightResponse.builder()
                            .roomId(Long.valueOf(item.getAmenityId()))
                            .name(amenity != null ? amenity.getName() : null)
                            .icon(amenity != null ? amenity.getIconName() : null)
                            .displayValue(item.getDisplayValue())
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional
    public void updateRoomAmenityHighlights(
            Long ownerId,
            Long homestayId,
            Long roomId,
            UpdateRoomAmenityHighlightsRequest request
    ) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        if (!homestay.getOwnerId().equals(ownerId)) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }

        HomestayRoom room = homestayRoomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ResultCode.ROOM_NOT_FOUND));

        if (!room.getHomestayId().equals(homestayId)) {
            throw new AppException(ResultCode.INVALID_INPUT);
        }

        List<RoomAmenityHighlightRequest> highlights = request.getHighlights() == null
                ? List.of()
                : request.getHighlights();

        Set<Integer> amenityIds = highlights.stream()
                .map(RoomAmenityHighlightRequest::getAmenityId)
                .collect(java.util.stream.Collectors.toSet());

        validateAmenityIds(amenityIds);

        roomAmenityHighlightRepository.deleteByRoomId(roomId);

        List<RoomAmenityHighlight> newItems = highlights.stream()
                .map(item -> {
                    RoomAmenityHighlight highlight = new RoomAmenityHighlight();
                    highlight.setRoomId(roomId);
                    highlight.setAmenityId(item.getAmenityId());
                    highlight.setDisplayValue(item.getDisplayValue());
                    return highlight;
                })
                .toList();

        roomAmenityHighlightRepository.saveAll(newItems);
    }
    private void validateAmenityIds(Set<Integer> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return;
        }

        long count = amenityRepository.findAllByIdIn(amenityIds).size();

        if (count != amenityIds.size()) {
            throw new AppException(ResultCode.INVALID_INPUT);
        }
    }
}
