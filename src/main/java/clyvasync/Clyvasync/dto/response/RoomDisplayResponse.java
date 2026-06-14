package clyvasync.Clyvasync.dto.response;


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
    private String type;
    private String description;
    private Integer maxGuests;
    private String areaM2;
    private Boolean hasPrivateBathroom;

    private BigDecimal price;

    private List<BedResponse> beds;
    private List<RoomImageResponse> images;
    private List<RatePlanResponse> ratePlans;
}
@Data @AllArgsConstructor @NoArgsConstructor
class BedResponse {
    private String type;
    private Integer quantity;
}

@Data @AllArgsConstructor @NoArgsConstructor
class RoomImageResponse {
    private Long id;
    private String url;
    private Boolean isCover;
}
