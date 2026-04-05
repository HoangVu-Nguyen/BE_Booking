package clyvasync.Clyvasync.infrastructure.storage.impl;

import clyvasync.Clyvasync.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3StorageServiceImpl implements FileStorageService {

    private final S3Client s3Client;

    @Value("${aws.bucket-name}")
    private String bucketName;

    @Value("${aws.cloudfront.domain}")
    private String cloudfrontDomain;

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        log.info("Starting upload file to S3: {}", file.getOriginalFilename());

        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .cacheControl("max-age=31536000") // Cache 1 năm cho static content trên CloudFront
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        String fileUrl = "https://" + cloudfrontDomain + "/" + fileName;
        log.info("Successfully uploaded file to S3. CloudFront URL: {}", fileUrl);

        return fileUrl;
    }

    @Override
    public void deleteFile(String fileUrl) {
        try {
            log.info("Attempting to delete file from S3: {}", fileUrl);

            String key = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());

            log.info("Successfully deleted file key: {} from S3 bucket: {}", key, bucketName);
        } catch (Exception e) {
            log.error("Failed to delete S3 object with URL: {}. Error: {}", fileUrl, e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int lastIndex = fileName.lastIndexOf(".");
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }
}
