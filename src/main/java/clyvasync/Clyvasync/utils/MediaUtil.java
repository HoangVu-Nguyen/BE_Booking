package clyvasync.Clyvasync.utils;

import clyvasync.Clyvasync.dto.request.UploadRequest;

import java.util.UUID;

public class MediaUtil {
    public static String generateObjectKey(String userId, UploadRequest request) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String safeFileName = request.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_");
        return String.format("users/%s/%s/%s-%s",
                userId,
                request.getImageType().name().toLowerCase(),
                uniqueSuffix,
                safeFileName);
    }
    public static String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf(".") > 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        }
        return "";
    }
}
