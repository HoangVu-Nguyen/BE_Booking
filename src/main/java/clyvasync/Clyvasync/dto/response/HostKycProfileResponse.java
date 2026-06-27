package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostKycProfileResponse {
    private String legalName;
    private String idCardNumber;
    private String idCardIssuedBy;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountOwner;
    private KycProfileStatus status;
    private String rejectionReason;
}