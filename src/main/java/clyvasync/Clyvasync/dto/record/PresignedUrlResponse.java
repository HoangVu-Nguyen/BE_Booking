package clyvasync.Clyvasync.dto.record;

import clyvasync.Clyvasync.enums.kyc.KycDocumentType;

import java.time.LocalDateTime;

public record PresignedUrlResponse (
        String url,
        String objectKey,
         String documentType,

        LocalDateTime expiresAt
){}


