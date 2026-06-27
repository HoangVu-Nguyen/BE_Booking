package clyvasync.Clyvasync.dto.event;

import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KycProcessedEvent {
    private final Long userId;
    private final KycProfileStatus status;
    private final String reason;
}