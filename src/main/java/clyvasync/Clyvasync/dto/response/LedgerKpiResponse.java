package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class LedgerKpiResponse {
    private BigDecimal totalGmv;        // Tổng GMV (từ bảng Bookings)
    private BigDecimal netRevenue;      // Doanh thu thuần (từ platform_fee_amount)
    private BigDecimal hostDebt;        // Dư nợ (từ bảng host_wallets)
    private BigDecimal totalRefunds;    // Hoàn tiền (từ wallet_transactions)
}
