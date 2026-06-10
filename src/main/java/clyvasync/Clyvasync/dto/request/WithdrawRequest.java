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

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;          // FE truyền lên: "Vietcombank"

    @NotBlank(message = "Số tài khoản không được để trống")
    private String accountNumber;     // FE truyền lên: "1023456789"

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String accountHolderName; // FE truyền lên: "NGUYEN VAN A"

    /**
     * MẸO KHÔN NGOAN: Tự động gom chuỗi chuẩn hóa để ném xuống DB cũ
     * Giúp giữ nguyên logic hàm logTransaction cũ mà không phải sửa cấu trúc bảng
     */
    public String toFormattedBankAccountInfo() {
        return String.format("%s - %s - %s",
                this.bankName.trim().toUpperCase(),
                this.accountNumber.trim(),
                this.accountHolderName.trim().toUpperCase()
        );
    }
}