package clyvasync.Clyvasync.controller.voucher;

import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.UserVoucherResponse;
import clyvasync.Clyvasync.dto.response.VoucherResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.voucher.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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
@RequestMapping("/api/v1/host")
@RequiredArgsConstructor
public class HostVoucherController {
    private final VoucherService voucherService;
    @GetMapping("/vouchers")
    public ApiResponse<List<VoucherResponse>> getHostVouchers(@CurrentUserId Long hostId) {
        return ApiResponse.success(voucherService.getHostVouchers(hostId));
    }

    @PostMapping("/vouchers")
    public ApiResponse<VoucherResponse> createHostVoucher(@Valid @RequestBody VoucherCreateRequest request, @CurrentUserId Long hostId) {
        return ApiResponse.success(voucherService.createHostVoucher(hostId, request));
    }

    @PutMapping("/{id}/deactivate")
    public ApiResponse<Void> deactivateHostVoucher(@PathVariable("id") Long id, @CurrentUserId Long hostId) {
        voucherService.deactivateHostVoucher(hostId, id);
        return ApiResponse.success();
    }
}

