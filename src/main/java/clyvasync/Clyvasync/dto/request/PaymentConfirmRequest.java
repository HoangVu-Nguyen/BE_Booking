package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentConfirmRequest {
    @NotNull(message = "Booking Code không được để trống")
    private String bookingCode;

    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // Hứng chuỗi: 'VNPAY', 'MOMO', 'TRANSFER', 'CARD_1', 'CARD_2'...
}