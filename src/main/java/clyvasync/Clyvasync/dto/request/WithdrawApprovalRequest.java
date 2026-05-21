package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WithdrawApprovalRequest {
    @NotNull(message = "ID giao dịch không được để trống")
    private Long transactionId;

    @NotBlank(message = "Trạng thái duyệt không được để trống")
    private String status; // "COMPLETED" hoặc "FAILED"

    private String adminComment; // Lý do từ chối nếu có
}