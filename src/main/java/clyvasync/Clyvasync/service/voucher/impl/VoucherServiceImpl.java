package clyvasync.Clyvasync.service.voucher.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.VoucherResponse;
import clyvasync.Clyvasync.modules.voucher.entity.VoucherTemplate;
import clyvasync.Clyvasync.repository.voucher.VoucherTemplateRepository;
import clyvasync.Clyvasync.service.voucher.VoucherService;
import clyvasync.Clyvasync.enums.offer.PointTransactionType;
import clyvasync.Clyvasync.enums.offer.VoucherStatus;
import clyvasync.Clyvasync.modules.voucher.entity.UserPointHistory;
import clyvasync.Clyvasync.modules.voucher.entity.UserVoucher;
import clyvasync.Clyvasync.repository.voucher.UserPointHistoryRepository;
import clyvasync.Clyvasync.repository.voucher.UserVoucherRepository;
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
    private final UserPointHistoryRepository userPointHistoryRepository;
    private final UserVoucherRepository userVoucherRepository;

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
    public Integer getCurrentUserPoints() {
        Long userId = getCurrentUserId();
        return userPointHistoryRepository.sumPointsByUserId(userId);
    }

    @Override
    @Transactional
    public void redeemVoucher(Long userId,Long templateId) {
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
            Integer currentPoints = userPointHistoryRepository.sumPointsByUserId(userId);
            if (currentPoints < requiredPoints) {
                throw new AppException(ResultCode.INSUFFICIENT_FUNDS);
            }
            
            UserPointHistory pointsDeduction = UserPointHistory.builder()
                    .userId(userId)
                    .points(-requiredPoints)
                    .transactionType(PointTransactionType.REDEEM)
                    .description("Đổi voucher " + template.getCode())
                    .build();
            userPointHistoryRepository.save(pointsDeduction);
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
