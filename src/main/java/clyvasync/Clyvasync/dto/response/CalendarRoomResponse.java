package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CalendarRoomResponse {
    private Long id; // Room ID
    private String name;
    private String tag;
    private BigDecimal basePrice;
    private List<CalendarInventoryResponse> inventory;
}