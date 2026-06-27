package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.dto.detail.HomestayDocumentMeta;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class HomestayBatchUploadRequest {
    @NotEmpty(message = "Danh sách tài liệu không được để trống")
    private List<HomestayDocumentMeta> items;
}