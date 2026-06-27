package clyvasync.Clyvasync.utils;

import clyvasync.Clyvasync.dto.detail.HomestayDocumentMeta;
import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import clyvasync.Clyvasync.dto.request.KycBatchUploadRequest;
import clyvasync.Clyvasync.dto.request.UploadRequest;
import clyvasync.Clyvasync.enums.media.ImageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MediaUtil {

    private final String cdnDomain;

    public MediaUtil(@Value("${aws.cloudfront.domain}") String cdnDomain) {
        this.cdnDomain = cdnDomain;
    }

    public String generateObjectKey(Long userId, UploadRequest request) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        String safeFileName = request.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_");

        return String.format("users/%s/%s/%s-%s",
                userId,
                request.getImageType().name().toLowerCase(),
                uniqueSuffix,
                safeFileName);
    }

    public String getFileExtension(String fileName) {
        if (fileName != null && fileName.lastIndexOf(".") > 0) {
            return fileName.substring(fileName.lastIndexOf("."));
        }

        return "";
    }

    public String toCdnUrl(String objectKey) {
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        if (objectKey.startsWith("http://") || objectKey.startsWith("https://")) {
            return objectKey;
        }

        if (cdnDomain == null || cdnDomain.isBlank()) {
            throw new IllegalStateException("aws.cloudfront.domain chưa được cấu hình");
        }

        String cleanDomain = cdnDomain.endsWith("/")
                ? cdnDomain.substring(0, cdnDomain.length() - 1)
                : cdnDomain;

        String cleanKey = objectKey.startsWith("/")
                ? objectKey.substring(1)
                : objectKey;

        return cleanDomain + "/" + cleanKey;
    }

    public List<String> toCdnUrls(List<String> objectKeys) {
        if (objectKeys == null) {
            return List.of();
        }

        return objectKeys.stream()
                .map(this::toCdnUrl)
                .toList();
    }
    public String generateObjectKey(Long userId, KycDocumentMeta request) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        String safeFileName = request.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_");

        return String.format("users/%s/%s/%s-%s",
                userId,
                ImageType.KYC_DOCUMENT.name().toLowerCase(),
                uniqueSuffix,
                safeFileName);
    }
    public String generateObjectKey(Long userId, HomestayDocumentMeta request) {
        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);

        String safeFileName = request.getFileName().replaceAll("[^a-zA-Z0-9.-]", "_");

        return String.format("users/%s/%s/%s-%s",
                userId,
                ImageType.KYC_DOCUMENT.name().toLowerCase(),
                uniqueSuffix,
                safeFileName);
    }
}