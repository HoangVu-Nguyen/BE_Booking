package clyvasync.Clyvasync.service.voucher;

import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.VoucherResponse;

import clyvasync.Clyvasync.dto.response.UserVoucherResponse;

import java.util.List;

public interface VoucherService {
    VoucherResponse createVoucher(VoucherCreateRequest request);
    List<VoucherResponse> getAllVouchers();
    Integer getCurrentUserPoints(Long userId);
    void redeemVoucher(Long userId,Long templateId);
    List<UserVoucherResponse> getMyVouchers(Long userId);
}
