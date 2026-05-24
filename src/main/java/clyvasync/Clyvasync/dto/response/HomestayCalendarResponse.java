package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomestayCalendarResponse {
    private String roomCode;
    private HomestayStatus status;

    private Long homestayId;
    private String homestayName;

    private List<CalendarRoomResponse> rooms;
    private OwnerResponse owner;
}
