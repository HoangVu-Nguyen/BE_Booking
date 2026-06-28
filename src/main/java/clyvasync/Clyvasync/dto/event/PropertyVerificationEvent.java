package clyvasync.Clyvasync.dto.event;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyVerificationEvent {
    private Long userId;
    private Long homestayId;
    private String homestayName;
    private HomestayStatus status;
}
