package clyvasync.Clyvasync.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmResponse {
    private String status;       // 'SUCCEEDED' (Thẻ), 'PENDING' (Chuyển khoản), 'REDIRECT' (VNPAY/MOMO)
    private String redirectUrl;  // URL chuyển hướng cho VNPAY/MOMO (nếu có)
    private String message;
}