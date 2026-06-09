package clyvasync.Clyvasync.service.media.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.media.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
@RequiredArgsConstructor
@Service
@Slf4j
public class S3ServiceImpl implements S3Service {
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}") private String bucketName;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    @Override
    public String generatePresignedPutUrl(String objectKey, String contentType, Long fileSize) {
        if (fileSize > MAX_FILE_SIZE) {
            throw new AppException(ResultCode.FILE_TOO_LARGE);
        }
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(fileSize)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(putObjectRequest)
                    .build();

            return s3Presigner.presignPutObject(presignRequest).url().toString();

        } catch (Exception e) {
            throw new AppException(ResultCode.UPLOAD_FAILED);
        }
    }

    @Override
    public void deleteFile(String objectKey) {

    }

    @Override
    public String getPublicUrl(String objectKey) {
        return "";
    }

    @Override
    public void deleteFiles(List<String> objectKeys) {

    }
}
