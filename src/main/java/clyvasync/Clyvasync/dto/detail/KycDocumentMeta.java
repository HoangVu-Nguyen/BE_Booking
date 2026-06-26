package clyvasync.Clyvasync.dto.detail;

import clyvasync.Clyvasync.enums.kyc.KycDocumentType;
import lombok.Data;

@Data
public class KycDocumentMeta {
    private String fileName;    // VD: cccd_mat_truoc.jpg
    private String contentType; // VD: image/jpeg
    private Long fileSize;      // Dung lượng file
    private KycDocumentType documentType; // ID_FRONT, PROPERTY_OWNERSHIP, v.v.
}