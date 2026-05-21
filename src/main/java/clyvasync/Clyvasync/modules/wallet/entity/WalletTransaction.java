package clyvasync.Clyvasync.modules.wallet.entity;

import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wallet_id", nullable = false)
    private Long walletId;

    @Column(name = "booking_id")
    private Long bookingId; // Có thể null nếu là lệnh rút tiền (WITHDRAWAL)

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal amount; // Số tiền cộng hoặc trừ

    // Các loại giao dịch: BOOKING_REVENUE (Cộng tiền phòng), WITHDRAWAL (Rút tiền), REFUND_DEDUCTION (Phạt/Hoàn tiền)
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 50, nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private TransactionStatus status;

    @Column(name = "bank_account_info", columnDefinition = "TEXT")
    private String bankAccountInfo; // Lưu thông tin ngân hàng lúc rút (VD: Vietcombank - 123456789 - NGUYEN VAN A)

    @Column(columnDefinition = "TEXT")
    private String description; // Lý do giao dịch (VD: "Cộng doanh thu từ booking #BK-987123")

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime createdAt;
}