package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SuspendHostRequest {
    @NotBlank(message = "Lý do đình chỉ không được để trống")
    private String reason;
    @NotNull(message = "Cần số ngày khóa")
    private Integer days;
}