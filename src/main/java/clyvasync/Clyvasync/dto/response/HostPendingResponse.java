package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class HostPendingResponse {
    private String id;
    private String name;
    private OffsetDateTime submittedAt;
    private Integer aiConfidence;
}