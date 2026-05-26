package clyvasync.Clyvasync.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancellationMailMessage {
    private String guestName;
    private String guestEmail;
    private String bookingCode;
    private String homestayName;
    private String checkInDate; // Có thể format dạng dd/MM/yyyy ở Service trước khi truyền vào
    private String cancelReason;
    private String totalPaid;
    private String refundAmount;
    private String penaltyFee;
    private String refundPolicyMessage;
}
