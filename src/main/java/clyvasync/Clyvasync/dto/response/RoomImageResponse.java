package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoomImageResponse {
    private Long id;
    private String url;
    private Boolean isCover;
    private int displayOrder;

    public RoomImageResponse(Long id, String url, Boolean isCover) {
        this.id = id;
        this.url = url;
        this.isCover = isCover;
    }
}