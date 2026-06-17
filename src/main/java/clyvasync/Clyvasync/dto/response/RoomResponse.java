package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RoomResponse {
    private Long id;
    private String name;
    private String description;
    private String tag;
    private String area;
    private String floor;
    private String wing;
    private String checkInTime;
    private Integer maxGuests;
    private Integer bedCount;
    private int availableQuantity;
    private boolean isInstantBook;

    private List<AmenityHighlightResponse> highlights;

    private List<RatePlanResponse> ratePlans;
    private List<RoomImageResponse> images;

    private BigDecimal basePrice;
    private List<BedResponse> beds;
    private List<CalendarInventoryResponse> inventory;
}