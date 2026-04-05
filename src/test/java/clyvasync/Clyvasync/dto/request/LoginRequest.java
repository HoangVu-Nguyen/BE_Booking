package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    private String password;

    @NotBlank(message = "DEVICE_ID_REQUIRED")
    private String deviceId;

    private String recaptcha;
}