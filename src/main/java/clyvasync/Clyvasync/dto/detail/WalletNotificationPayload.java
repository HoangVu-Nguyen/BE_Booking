package clyvasync.Clyvasync.dto.detail;

import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletNotificationPayload {
    private TransactionType type;            // VD: "WITHDRAW_APPROVED", "WITHDRAW_REJECTED"
    private Long transactionId;
    private BigDecimal amount;
    private TransactionStatus status;          // COMPLETED hoặc FAILED
    private String message;
}
