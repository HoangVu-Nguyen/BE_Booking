package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CardConfirmRequest {

    @NotBlank(message = "Payment Method ID không được để trống")
    private String paymentMethodId; // Hứng chuỗi 'pm_1Pxxxx...' từ Angular gửi lên
}