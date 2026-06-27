package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycBatchUploadRequest {
    private Long profileId;
    private List<KycDocumentMeta> items;
}