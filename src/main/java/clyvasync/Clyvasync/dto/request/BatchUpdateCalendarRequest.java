package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.enums.calendar.RoomCalendarStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class BatchUpdateCalendarRequest {

    private Long roomId;

    private LocalDate startDate;

    private LocalDate endDate;

    /**
     * price | inventory | status
     */
    private String actionType;

    /**
     * Dùng cho tab tồn kho hoặc tab status
     */
    private Integer availableQuantity;

    /**
     * AVAILABLE, BLOCKED, MAINTENANCE
     */
    private RoomCalendarStatus status;

    /**
     * Dùng cho tab price
     */
    private List<RatePlanUpdateRequest> ratePlanUpdates;

    private Integer minNights;

    private BigDecimal weekendFee;

    private Boolean syncOta;

    private String blockReason;

    private String internalNote;

    private Boolean createTaskForTeam;

    private Long operatorId;

    private String updatedAt;

    @Data
    public static class RatePlanUpdateRequest {
        private Long ratePlanId;
        private BigDecimal price;
    }
}