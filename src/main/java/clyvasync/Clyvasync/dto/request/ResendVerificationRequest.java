package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.enums.otp.OtpType;
import lombok.Data;

@Data
public class ResendVerificationRequest {
    private String email;
    private OtpType type;
}
