package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.room.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDisplayResponse {
    private Long id;
    private String name;
    private RoomType type;
    private String description;
    private Integer maxGuests;
    private String areaM2;
    private Boolean hasPrivateBathroom;

    private BigDecimal price;

    private List<BedResponse> beds;
    private List<RoomImageResponse> images;
    private List<RatePlanResponse> ratePlans;
}


