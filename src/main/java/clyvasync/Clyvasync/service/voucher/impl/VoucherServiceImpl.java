package clyvasync.Clyvasync.service.voucher.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.VoucherResponse;
import clyvasync.Clyvasync.modules.voucher.entity.VoucherTemplate;
import clyvasync.Clyvasync.repository.voucher.VoucherTemplateRepository;
import clyvasync.Clyvasync.service.voucher.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherTemplateRepository voucherTemplateRepository;

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
