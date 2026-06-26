package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class HostKycProfileRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 3, max = 255, message = "Họ tên từ 3 đến 255 ký tự")
    private String legalName;

    @NotBlank(message = "Số CCCD không được để trống")
    @Pattern(regexp = "^[0-9]{9,12}$", message = "Số CCCD không hợp lệ")
    private String idCardNumber;

    @NotBlank(message = "Nơi cấp không được để trống")
    private String idCardIssuedBy;

    @NotBlank(message = "Tên ngân hàng không được để trống")
    private String bankName;

    @NotBlank(message = "Số tài khoản ngân hàng không được để trống")
    private String bankAccountNumber;

    @NotBlank(message = "Tên chủ tài khoản không được để trống")
    private String bankAccountOwner;
}
