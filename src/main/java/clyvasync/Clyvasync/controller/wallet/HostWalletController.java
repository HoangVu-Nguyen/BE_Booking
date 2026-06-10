package clyvasync.Clyvasync.controller.wallet.controller;

import clyvasync.Clyvasync.dto.request.WithdrawRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.modules.wallet.entity.HostWallet;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.service.wallet.WalletTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/host/wallet")
@RequiredArgsConstructor
public class HostWalletController {

    private final HostWalletService hostWalletService;
    private final WalletTransactionService walletTransactionService;

    // 1. Lấy thông tin Ví (Số dư khả dụng, Số dư chờ duyệt...)
    @GetMapping
    public ApiResponse<HostWallet> getMyWallet(@CurrentUserId Long currentHostId) {
        return ApiResponse.success(hostWalletService.getWalletInfo(currentHostId));
    }

    // 2. Host gửi yêu cầu Rút Tiền
    @PostMapping("/withdraw")
    public ApiResponse<Void> requestWithdraw(
            @CurrentUserId Long userId,
            @Valid @RequestBody WithdrawRequest request) {

        // Bóc tách amount và gọi hàm nối chuỗi thông minh từ DTO
        hostWalletService.requestWithdrawal(
                userId,
                request.getAmount(),
                request.toFormattedBankAccountInfo()
        );

        return ApiResponse.success(null);
    }

    // 3. Xem lịch sử dòng tiền (Cộng tiền phòng, trừ tiền rút...)
    @GetMapping("/transactions")
    public ApiResponse<Page<WalletTransaction>> getTransactionHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,@CurrentUserId Long currentHostId ) {
        HostWallet wallet = hostWalletService.getWalletInfo(currentHostId);
        return ApiResponse.success(walletTransactionService.getTransactionHistory(wallet.getId(), PageRequest.of(page, size)));
    }
}