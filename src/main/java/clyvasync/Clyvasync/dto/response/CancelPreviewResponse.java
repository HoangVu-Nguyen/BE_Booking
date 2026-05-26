package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CancelPreviewResponse {
    private String bookingCode;
    private BigDecimal totalPaid;          // Tổng tiền khách đã trả
    private BigDecimal refundAmount;       // Tiền khách sẽ nhận lại
    private BigDecimal penaltyFee;         // Tiền phạt (chủ nhà hưởng)
    private String refundPolicyMessage;    // Dòng text giải thích (VD: "Hủy trước 3 ngày, hoàn 50%")
}
