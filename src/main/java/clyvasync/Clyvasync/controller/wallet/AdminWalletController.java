package clyvasync.Clyvasync.controller.wallet;

import clyvasync.Clyvasync.dto.request.WithdrawApprovalRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.service.wallet.WalletTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final HostWalletService hostWalletService;
    private final WalletTransactionService walletTransactionService;

    // Admin xử lý các lệnh rút tiền đang chờ duyệt
    @PostMapping("/withdrawals/resolve")
    public ApiResponse<String> resolveWithdrawalRequest(@Valid @RequestBody WithdrawApprovalRequest request) {
        hostWalletService.resolveWithdrawal(request);
        return ApiResponse.success();
    }
    @GetMapping("/withdrawals/pending")
    public ApiResponse<Page<WalletTransaction>> getPendingWithdrawals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ApiResponse.success(walletTransactionService
                .findByTransactionTypeAndStatus(TransactionType.WITHDRAWAL, TransactionStatus.PENDING, PageRequest.of(page, size)));
    }
}