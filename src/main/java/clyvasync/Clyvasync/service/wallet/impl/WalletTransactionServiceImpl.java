package clyvasync.Clyvasync.service.wallet.impl;

import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import clyvasync.Clyvasync.repository.wallet.WalletTransactionRepository;
import clyvasync.Clyvasync.service.wallet.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;

    @Override
    @Transactional
    public WalletTransaction logTransaction(Long walletId, Long bookingId, BigDecimal amount,
                                            TransactionType type, TransactionStatus status, String description,
                                            String bankAccountInfo) {
        // Tạo một bản ghi giao dịch mới sử dụng Builder pattern
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(walletId)
                .bookingId(bookingId)
                .amount(amount)
                .transactionType(type)
                .status(status)
                .description(description)
                .bankAccountInfo(bankAccountInfo)
                .build();

        // Lưu vào Database và trả về Entity đã lưu
        return walletTransactionRepository.save(transaction);
    }

    @Override
    public Page<WalletTransaction> getTransactionHistory(Long walletId, Pageable pageable) {
        // Trả về lịch sử giao dịch (thường sẽ được sort giảm dần theo createdAt từ Controller)
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }

    @Override
    public Page<WalletTransaction> getTransactionHistoryByType(Long walletId, TransactionType type, Pageable pageable) {
        // Lọc giao dịch theo loại (VD: Chỉ lấy các lệnh rút tiền 'WITHDRAWAL')
        return walletTransactionRepository.findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(walletId, type, pageable);
    }

    @Override
    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus status) {
        // Tìm giao dịch, nếu không có thì ném lỗi
        WalletTransaction transaction = walletTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch mã: " + transactionId));

        // Cập nhật trạng thái mới (VD: PENDING -> COMPLETED)
        transaction.setStatus(status);
        walletTransactionRepository.save(transaction);
    }

    @Override
    public WalletTransaction getById(Long id) {
        return walletTransactionRepository.findById(id).orElseThrow(() -> new AppException(ResultCode.TRANSACTION_NOT_FOUND));
    }

    @Override
    public void save(WalletTransaction walletTransaction) {
        walletTransactionRepository.save(walletTransaction);
    }

    @Override
    public Page<WalletTransaction> findByTransactionTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable) {
        return walletTransactionRepository.findByTransactionTypeAndStatus(type,status,pageable);
    }
}