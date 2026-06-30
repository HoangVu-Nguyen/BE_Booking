package clyvasync.Clyvasync.controller.voucher;
import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.VoucherResponse;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.exception.ResultCode;

import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.voucher.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public ApiResponse<VoucherResponse> createVoucher(@Valid @RequestBody VoucherCreateRequest request) {
        VoucherResponse response = voucherService.createVoucher(request);
        return ApiResponse.success(response);
    }

    @GetMapping
    public ApiResponse<List<VoucherResponse>> getAllVouchers() {
        List<VoucherResponse> responses = voucherService.getAllVouchers();
        return ApiResponse.success(responses);
    }

    @GetMapping("/points/me")
    public ApiResponse<Integer> getCurrentUserPoints() {
        return ApiResponse.success(voucherService.getCurrentUserPoints());
    }

    @PostMapping("/{id}/redeem")
    public ApiResponse<Void> redeemVoucher(@PathVariable("id") Long templateId, @CurrentUserId Long userId) {
        voucherService.redeemVoucher(userId,templateId);
        return ApiResponse.success();
    }
}