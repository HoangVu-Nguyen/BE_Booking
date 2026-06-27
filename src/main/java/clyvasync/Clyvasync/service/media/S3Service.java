package clyvasync.Clyvasync.service.media;

import java.util.List;

public interface S3Service {
    String generatePresignedPutUrl(String objectKey, String contentType, Long fileSize);
    void deleteFile(String objectKey);
    String getPublicUrl(String objectKey) ;
    void deleteFiles(List<String> objectKeys);
    boolean doesFileExist(String objectKey);
    byte[] downloadFileAsBytes(String objectKey);
}