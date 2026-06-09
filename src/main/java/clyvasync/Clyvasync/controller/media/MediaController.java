package clyvasync.Clyvasync.controller.media;

import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.media.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaController {
    private final S3Service s3Service;
    @PostMapping("/presigned-url/batch")
    public ApiResponse<List<PresignedUrlResponse>> getBatchUploadUrls(
            @RequestBody @Valid BatchUploadRequest request,
            @CurrentUserId String userId) {
        return ApiResponse.success(s3Service.generatePresignedPutUrl(userId, request));
    }
}
