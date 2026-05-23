package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.enums.room.RoomStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BatchUpdateCalendarRequest {
    private Long roomId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal priceOverride; // null nếu không đổi giá
    private Integer availableQuantity; // null nếu không đổi kho
    private RoomStatus status; // 'AVAILABLE', 'BLOCKED', 'MAINTENANCE'
    private String internalNote; // Để lưu vết
}