package clyvasync.Clyvasync.controller.voucher;
import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.VoucherResponse;
import clyvasync.Clyvasync.dto.response.UserVoucherResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
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
    public ApiResponse<Integer> getCurrentUserPoints(@CurrentUserId  Long userId) {
        return ApiResponse.success(voucherService.getCurrentUserPoints(userId));
    }

    @GetMapping("/my-vouchers")
    public ApiResponse<List<UserVoucherResponse>> getMyVouchers(@CurrentUserId Long userId) {
        return ApiResponse.success(voucherService.getMyVouchers(userId));
    }

    @GetMapping("/applicable")
    public ApiResponse<List<UserVoucherResponse>> getApplicableVouchers(
            @CurrentUserId Long userId,
            @org.springframework.web.bind.annotation.RequestParam String bookingCode) {
        return ApiResponse.success(voucherService.getApplicableVouchers(userId, bookingCode));
    }

    @PostMapping("/{id}/redeem")
    public ApiResponse<Void> redeemVoucher(@PathVariable("id") Long templateId, @CurrentUserId Long userId) {
        voucherService.redeemVoucher(userId,templateId);
        return ApiResponse.success();
    }

    @GetMapping("/host")
    public ApiResponse<List<VoucherResponse>> getHostVouchers(@CurrentUserId Long hostId) {
        return ApiResponse.success(voucherService.getHostVouchers(hostId));
    }

    @PostMapping("/host/voucher")
    public ApiResponse<VoucherResponse> createHostVoucher(@Valid @RequestBody VoucherCreateRequest request, @CurrentUserId Long hostId) {
        return ApiResponse.success(voucherService.createHostVoucher(hostId, request));
    }

    @PutMapping("/host/{id}/deactivate")
    public ApiResponse<Void> deactivateHostVoucher(@PathVariable("id") Long id, @CurrentUserId Long hostId) {
        voucherService.deactivateHostVoucher(hostId, id);
        return ApiResponse.success();
    }
}