package clyvasync.Clyvasync.dto.summary;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomestayRoomSummary {
    private Long homestayId;
    private BigDecimal minPrice;
    private Integer maxGuestsInRoom;
    private Integer totalRooms;
}