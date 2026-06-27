package clyvasync.Clyvasync.dto.detail;

import clyvasync.Clyvasync.enums.homestay.DocumentType;
import clyvasync.Clyvasync.enums.homestay.PropertyDocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HomestayDocumentMeta {
    @NotNull
    private PropertyDocumentType documentType;

    @NotBlank
    private String fileName;

    @NotBlank
    private String contentType;

    private Long fileSize;
}