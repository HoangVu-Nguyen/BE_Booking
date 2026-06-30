package clyvasync.Clyvasync.dto.event;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomestayStatusChangedEvent {
    private final Long userId; // ID của Host
    private final Long homestayId;
    private final String homestayName;
    private final HomestayStatus newStatus;
    private final String reason;
}