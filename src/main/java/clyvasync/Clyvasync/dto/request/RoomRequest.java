package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.dto.response.BedResponse;
import clyvasync.Clyvasync.dto.response.RoomImageResponse;
import lombok.Data;

import java.util.List;
@Data
public class RoomRequest {
    private Long id; // null nếu là phòng mới
    private String name;
    private String type;
    private String description;
    private Integer maxGuests;
    private Double areaM2;
    private Boolean hasPrivateBathroom;
    private List<BedResponse> beds;
    private List<ImageRequest> images; // Chứa info ảnh cũ hoặc đánh dấu ảnh mới
}