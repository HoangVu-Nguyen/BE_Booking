package clyvasync.Clyvasync.service.media;

import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.enums.homestay.PropertyDocumentType;
import clyvasync.Clyvasync.enums.kyc.KycDocumentType;

import java.util.List;

public interface S3Service {
    String generatePresignedPutUrl(String objectKey, String contentType, Long fileSize);
    void deleteFile(String objectKey);
    String getPublicUrl(String objectKey) ;
    void deleteFiles(List<String> objectKeys);
    boolean doesFileExist(String objectKey);
    byte[] downloadFileAsBytes(String objectKey);
    PresignedUrlResponse generatePresignedUrl(String objectKey, KycDocumentType type);
    PresignedUrlResponse generatePresignedUrl(String objectKey, PropertyDocumentType type);
}