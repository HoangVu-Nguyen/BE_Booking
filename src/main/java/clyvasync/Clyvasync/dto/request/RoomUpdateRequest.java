package clyvasync.Clyvasync.dto.request;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomUpdateRequest {
    private Long id; // null = tạo mới
    private String name;
    private String type;
    private String description;
    private Integer maxGuests;
    private String area;
    private Boolean hasPrivateBathroom;
    private Integer sortOrder;

    private List<BedRequest> beds;
    private List<ImageSubmitRequest> images;
}