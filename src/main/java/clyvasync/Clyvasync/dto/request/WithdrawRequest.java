package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class WithdrawRequest {
    @NotNull(message = "Số tiền rút không được để trống")
    @DecimalMin(value = "50000.00", message = "Số tiền rút tối thiểu là 50,000 VNĐ")
    private BigDecimal amount;

    @NotBlank(message = "Thông tin tài khoản ngân hàng không được để trống")
    private String bankAccountInfo; // VD: "Vietcombank - 1023456789 - NGUYEN VAN A"
}