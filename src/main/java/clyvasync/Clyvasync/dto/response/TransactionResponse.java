package clyvasync.Clyvasync.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String id;
    // Mã TXN (VD: TXN-12345)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;         // Thời gian giao dịch
    private String type;                // PAYMENT_IN, PAYOUT_OUT, REFUND
    private GuestDto guest;
    private HostDto host;
    private PaymentDetailsDto paymentDetails;
    private AmountsDto amounts;
    private String status;              // COMPLETED, PENDING, FAILED

    // --- Các class con lồng bên trong ---
    @Data @Builder public static class GuestDto {
        private String name;
        private String avatar;
    }

    @Data @Builder public static class HostDto {
        private String name;
    }

    @Data @Builder public static class PaymentDetailsDto {
        private String method;          // VNPay, Momo, Bank Transfer
        private String bank;
        private String last4;
    }

    @Data @Builder public static class AmountsDto {
        private BigDecimal gross;       // Tổng tiền khách trả
        private BigDecimal platformFee; // Phí sàn
        private BigDecimal netToHost;   // Tiền Host nhận / Rút ra
    }
}