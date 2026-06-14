package clyvasync.Clyvasync.dto.request;


import lombok.Data;

import java.util.List;

@Data
public class BatchUploadRequest {
    private Long targetId;
    private List<UploadRequest> items;
}
