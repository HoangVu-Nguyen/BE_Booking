package clyvasync.Clyvasync.service.voucher.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.VoucherResponse;
import clyvasync.Clyvasync.dto.response.UserVoucherResponse;
import clyvasync.Clyvasync.modules.voucher.entity.VoucherTemplate;
import clyvasync.Clyvasync.repository.voucher.VoucherTemplateRepository;
import clyvasync.Clyvasync.service.voucher.VoucherService;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.service.voucher.PointService;
import clyvasync.Clyvasync.enums.offer.PointTransactionType;
import clyvasync.Clyvasync.enums.offer.VoucherStatus;
import clyvasync.Clyvasync.modules.voucher.entity.UserPointHistory;
import clyvasync.Clyvasync.modules.voucher.entity.UserVoucher;
import clyvasync.Clyvasync.repository.voucher.UserPointHistoryRepository;
import clyvasync.Clyvasync.repository.voucher.UserVoucherRepository;
import clyvasync.Clyvasync.modules.voucher.entity.HostVoucherApplyScope;
import clyvasync.Clyvasync.repository.voucher.HostVoucherApplyScopeRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherTemplateRepository voucherTemplateRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final PointService pointService;
    private final HostVoucherApplyScopeRepository hostVoucherApplyScopeRepository;
    private final HomestayRepository homestayRepository;

    @Override
    @Transactional
    public VoucherResponse createVoucher(VoucherCreateRequest request) {
        if (request.getCode() != null && voucherTemplateRepository.existsByCode(request.getCode())) {
            throw new AppException(ResultCode.DATA_ERROR);
        }

        VoucherTemplate template = VoucherTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minOrderValue(request.getMinOrderValue())
                .pointsRequired(request.getPointsRequired() != null ? request.getPointsRequired() : 0)
                .sponsorType(request.getSponsorType())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .totalIssueLimit(request.getTotalIssueLimit())
                .totalUsageLimit(request.getTotalUsageLimit())
                .currentIssueCount(0)
                .currentUsageCount(0)
                .isActive(true)
                .build();

        template = voucherTemplateRepository.save(template);

        return mapToResponse(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getAllVouchers() {
        return voucherTemplateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Integer getCurrentUserPoints(Long userId) {
        return userRepository.findById(userId)
                .map(User::getRewardPoints)
                .orElse(0);
    }

    @Override
    @Transactional
    public void redeemVoucher(Long userId, Long templateId) {
        VoucherTemplate template = voucherTemplateRepository.findById(templateId)
                .orElseThrow(() -> new AppException(ResultCode.DATA_NOT_FOUND));

        if (!Boolean.TRUE.equals(template.getIsActive())) {
            throw new AppException(ResultCode.DATA_ERROR);
        }
        if (template.getValidUntil() != null && template.getValidUntil().isBefore(OffsetDateTime.now())) {
            throw new AppException(ResultCode.DATA_ERROR);
        }
        if (template.getTotalIssueLimit() != null && template.getCurrentIssueCount() >= template.getTotalIssueLimit()) {
            throw new AppException(ResultCode.DATA_ERROR);
        }

        Integer requiredPoints = template.getPointsRequired() != null ? template.getPointsRequired() : 0;
        
        if (requiredPoints > 0) {
            pointService.deductPointsForVoucher(userId, requiredPoints, "Đổi voucher " + template.getCode());
        }
        
        UserVoucher userVoucher = UserVoucher.builder()
                .userId(userId)
                .templateId(templateId)
                .status(VoucherStatus.AVAILABLE)
                .build();
        userVoucherRepository.save(userVoucher);
        
        template.setCurrentIssueCount(template.getCurrentIssueCount() + 1);
        voucherTemplateRepository.save(template);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVoucherResponse> getMyVouchers(Long userId) {
        List<UserVoucher> userVouchers = userVoucherRepository.findByUserId(userId);
        return userVouchers.stream().map(userVoucher -> {
            VoucherTemplate template = voucherTemplateRepository.findById(userVoucher.getTemplateId())
                    .orElse(null);
            
            return UserVoucherResponse.builder()
                    .id(userVoucher.getId())
                    .code(template != null ? template.getCode() : null)
                    .title(template != null ? template.getName() : null)
                    .discountValue(template != null ? template.getDiscountValue() : null)
                    .discountType(template != null ? template.getDiscountType() : null)
                    .validUntil(template != null ? template.getValidUntil() : null)
                    .status(userVoucher.getStatus().name())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VoucherResponse> getHostVouchers(Long hostId) {
        return voucherTemplateRepository.findHostVouchers(hostId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public VoucherResponse createHostVoucher(Long hostId, VoucherCreateRequest request) {
        if (request.getCode() != null && voucherTemplateRepository.existsByCode(request.getCode())) {
            throw new AppException(ResultCode.DATA_ERROR);
        }

        VoucherTemplate template = VoucherTemplate.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minOrderValue(request.getMinOrderValue())
                .pointsRequired(request.getPointsRequired() != null ? request.getPointsRequired() : 0)
                .sponsorType(request.getSponsorType())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .totalIssueLimit(request.getTotalIssueLimit())
                .totalUsageLimit(request.getTotalUsageLimit())
                .currentIssueCount(0)
                .currentUsageCount(0)
                .isActive(true)
                .build();

        template = voucherTemplateRepository.save(template);

        List<Long> homestayIds;
        if (Boolean.TRUE.equals(request.getIsApplyAll())) {
            homestayIds = homestayRepository.findAllByOwnerId(hostId).stream()
                    .map(Homestay::getId)
                    .collect(Collectors.toList());
        } else {
            homestayIds = request.getApplicableHomestayIds();
            if (homestayIds != null) {
                // Validate all homestays belong to the host
                for (Long homestayId : homestayIds) {
                    if (!homestayRepository.existsByIdAndOwnerId(homestayId, hostId)) {
                        throw new AppException(ResultCode.DATA_ERROR);
                    }
                }
            }
        }

        if (homestayIds != null) {
            for (Long homestayId : homestayIds) {
                HostVoucherApplyScope scope = HostVoucherApplyScope.builder()
                        .voucherId(template.getId())
                        .homestayId(homestayId)
                        .build();
                hostVoucherApplyScopeRepository.save(scope);
            }
        }

        return mapToResponse(template);
    }

    @Override
    @Transactional
    public void deactivateHostVoucher(Long hostId, Long voucherId) {
        VoucherTemplate template = voucherTemplateRepository.findById(voucherId)
                .orElseThrow(() -> new AppException(ResultCode.DATA_NOT_FOUND));

        // Check if host owns this voucher
        List<HostVoucherApplyScope> scopes = hostVoucherApplyScopeRepository.findByVoucherId(voucherId);
        if (scopes.isEmpty()) {
            throw new AppException(ResultCode.DATA_ERROR);
        }
        
        Long homestayId = scopes.get(0).getHomestayId();
        if (!homestayRepository.existsByIdAndOwnerId(homestayId, hostId)) {
            throw new AppException(ResultCode.DATA_ERROR);
        }

        template.setIsActive(false);
        voucherTemplateRepository.save(template);
    }


    private VoucherResponse mapToResponse(VoucherTemplate template) {
        return VoucherResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .description(template.getDescription())
                .discountType(template.getDiscountType())
                .discountValue(template.getDiscountValue())
                .maxDiscount(template.getMaxDiscount())
                .minOrderValue(template.getMinOrderValue())
                .pointsRequired(template.getPointsRequired())
                .sponsorType(template.getSponsorType())
                .validFrom(template.getValidFrom())
                .validUntil(template.getValidUntil())
                .isActive(template.getIsActive())
                .totalIssueLimit(template.getTotalIssueLimit())
                .currentIssueCount(template.getCurrentIssueCount())
                .totalUsageLimit(template.getTotalUsageLimit())
                .currentUsageCount(template.getCurrentUsageCount())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
