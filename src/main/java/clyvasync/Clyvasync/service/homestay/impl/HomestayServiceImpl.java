package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.response.AmenityResponse;
import clyvasync.Clyvasync.dto.response.HomestayDetailResponse;
import clyvasync.Clyvasync.dto.response.HomestayResponse;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.homestay.AmenityMapper;
import clyvasync.Clyvasync.mapper.homestay.HomestayMapper;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import clyvasync.Clyvasync.repository.homestay.AmenityRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayImageRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomestayServiceImpl implements HomestayService {

    private final HomestayRepository homestayRepository;
    private final HomestayMapper homestayMapper;
    private final HomestayImageRepository imageRepository;
    private final AmenityRepository amenityRepository;
    private final AmenityMapper amenityMapper;

    @Override
    @Transactional
    public HomestayResponse createHomestay(HomestayRequest request, Long ownerId) {
        Homestay homestay = homestayMapper.toHomestay(request);
        homestay.setOwnerId(ownerId);

        if (homestay.getImages() != null) {
            homestay = homestayRepository.save(homestay);
            Long newHomestayId = homestay.getId();
            homestay.getImages().forEach(img -> img.setHomestayId(newHomestayId));
        }

        Homestay saved = homestayRepository.save(homestay);
        return homestayMapper.toHomestayResponse(saved);
    }

    @Override
    @Transactional
    public HomestayResponse updateHomestay(Long id, HomestayRequest request, Long ownerId) {
        Homestay homestay = homestayRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        homestayMapper.updateHomestayFromRequest(request, homestay);

        if (homestay.getImages() != null) {
            homestay.getImages().forEach(img -> img.setHomestayId(homestay.getId()));
        }

        Homestay updated = homestayRepository.save(homestay);
        return homestayMapper.toHomestayResponse(updated);
    }

    @Override
    @Transactional
    public void deleteHomestay(Long id, Long ownerId) {
        Homestay homestay = homestayRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        homestay.setDeletedAt(LocalDateTime.now());
        homestayRepository.save(homestay);
    }

    @Override
    @Transactional(readOnly = true)
    public HomestayResponse getById(Long id) {
        return homestayRepository.findByIdAndDeletedAtIsNull(id)
                .map(homestayMapper::toHomestayResponse)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HomestayResponse> searchHomestays(String city, BigDecimal minPrice, BigDecimal maxPrice, Integer guests, Pageable pageable) {

        // Tiền xử lý: Thêm % và chuyển thành CHỮ THƯỜNG ngay tại Java
        String searchCity = (city != null && !city.trim().isEmpty())
                ? "%" + city.trim().toLowerCase() + "%"
                : null;

        return homestayRepository.searchHomestays(searchCity, minPrice, maxPrice, guests, pageable)
                .map(homestayMapper::toHomestayResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomestayResponse> getByOwnerId(Long ownerId) {
        return homestayRepository.findAllByOwnerIdAndDeletedAtIsNull(ownerId)
                .stream()
                .map(homestayMapper::toHomestayResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(Long id, String status, Long ownerId) {
        Homestay homestay = homestayRepository.findByIdAndOwnerIdAndDeletedAtIsNull(id, ownerId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        homestay.setStatus(HomestayStatus.valueOf(status));
        homestayRepository.save(homestay);
    }

    @Override
    @Transactional
    public void updateAverageRating(Long id, BigDecimal newRating) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        BigDecimal oldCount = BigDecimal.valueOf(homestay.getReviewCount());
        BigDecimal totalScore = homestay.getAverageRating().multiply(oldCount).add(newRating);

        homestay.setReviewCount(homestay.getReviewCount() + 1);
        homestay.setAverageRating(totalScore.divide(BigDecimal.valueOf(homestay.getReviewCount()), 2, RoundingMode.HALF_UP));

        homestayRepository.save(homestay);
    }

    @Override
    public HomestayDetailResponse getHomestayDetail(Long id) {
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));
        List<String> imageUrls = imageRepository.findByHomestayIdOrderByDisplayOrderAsc(id)
                .stream()
                .map(HomestayImage::getImageUrl)
                .toList();
        List<AmenityResponse> amenities = amenityMapper.toAmenityResponseList(amenityRepository.findAllByHomestayId(id));
        return HomestayDetailResponse.builder()
                .id(homestay.getId())
                .name(homestay.getName())
                .description(homestay.getDescription())
                .address(homestay.getAddress())
                .city(homestay.getCity())
                .latitude(homestay.getLatitude())
                .longitude(homestay.getLongitude())
                .basePrice(homestay.getBasePrice())
                .averageRating(homestay.getAverageRating())
                .reviewCount(homestay.getReviewCount())
                .ownerId(homestay.getOwnerId())
                .imageUrls(imageUrls)
                .amenities(amenities)
                .build();
    }

}