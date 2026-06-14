package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RoomUpdateResponse {
    private String message;
    private List<PresignedUrlResponse> uploadUrls; // Trả về list link cho FE upload
}
