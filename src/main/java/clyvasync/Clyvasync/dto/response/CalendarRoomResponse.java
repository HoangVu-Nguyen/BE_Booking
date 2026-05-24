package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CalendarRoomResponse {
    private Long id;
    private String imageUrl;
    private String name;
    private String tag;
    private BigDecimal basePrice;
    private List<CalendarInventoryResponse> inventory;
}