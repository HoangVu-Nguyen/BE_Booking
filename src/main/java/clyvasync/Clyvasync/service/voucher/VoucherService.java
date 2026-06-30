package clyvasync.Clyvasync.service.voucher;

import clyvasync.Clyvasync.dto.request.VoucherCreateRequest;
import clyvasync.Clyvasync.dto.response.VoucherResponse;

import java.util.List;

public interface VoucherService {
    VoucherResponse createVoucher(VoucherCreateRequest request);
    List<VoucherResponse> getAllVouchers();
}
