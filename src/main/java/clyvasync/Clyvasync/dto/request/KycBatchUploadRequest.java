package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import lombok.Data;
import java.util.List;

@Data
public class KycBatchUploadRequest {
    private Long profileId;
    private List<KycDocumentMeta> items;
}