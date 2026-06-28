package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.kyc.KycDocumentStatus;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class HostPendingResponse {
    private Long profileId;
    private String name;
    private Integer aiConfidence;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime submittedAt;
    private KycProfileStatus status;
}