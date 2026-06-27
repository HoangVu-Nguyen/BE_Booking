package clyvasync.Clyvasync.service.media.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.media.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class S3ServiceImpl implements S3Service {
    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    @Value("${aws.s3.bucket-name}") private String bucketName;
    private static final long MAX_FILE_SIZE = 70 * 1024 * 1024; //70MB
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
        if (objectKey == null || objectKey.trim().isEmpty()) {
            log.warn(">>>> [S3] Attempted to delete file with null or empty objectKey");
            return;
        }

        try {
            // 2. Build Request
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            // 3. Thực thi
            s3Client.deleteObject(deleteObjectRequest);
            log.info(">>>> [S3] Successfully deleted file: {}", objectKey);

        } catch (S3Exception e) {
            // Lỗi xuất phát từ phía AWS (VD: sai quyền IAM)
            log.error(">>>> [S3] AWS Error deleting file {}: {}", objectKey, e.awsErrorDetails().errorMessage());
            throw new AppException(ResultCode.DELETE_FAILED);
        } catch (Exception e) {
            // Lỗi mạng hoặc lỗi hệ thống khác
            log.error(">>>> [S3] Unexpected error deleting file {}: {}", objectKey, e.getMessage());
            throw new AppException(ResultCode.DELETE_FAILED);
        }
    }

    @Override
    public String getPublicUrl(String objectKey) {
        return objectKey;
    }
    @Override
    public void deleteFiles(List<String> objectKeys) {
        if (objectKeys == null || objectKeys.isEmpty()) return;

        try {
            List<ObjectIdentifier> identifiers = objectKeys.stream()
                    .map(key -> ObjectIdentifier.builder().key(key).build())
                    .collect(Collectors.toList());

            // Build request xóa hàng loạt
            Delete delete = Delete.builder().objects(identifiers).build();
            DeleteObjectsRequest deleteReq = DeleteObjectsRequest.builder()
                    .bucket(bucketName)
                    .delete(delete)
                    .build();

            // Bắn 1 request duy nhất lên S3
            s3Client.deleteObjects(deleteReq);
            log.info(">>>> [S3] Batch deleted {} files successfully.", objectKeys.size());

        } catch (Exception e) {
            log.error(">>>> [S3] Error during batch delete: {}", e.getMessage());
            throw new AppException(ResultCode.DELETE_FAILED);
        }
    }
    @Override
    public boolean doesFileExist(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error(">>>> [S3] Lỗi khi kiểm tra file tồn tại: {}", e.getMessage());
            return false;
        }
    }
    @Override
    public byte[] downloadFileAsBytes(String objectKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);
            return s3Object.readAllBytes();
        } catch (Exception e) {
            log.error(">>>> [S3] Lỗi khi tải file: {}", e.getMessage());
            throw new RuntimeException("Không thể tải ảnh từ S3");
        }
    }
}
