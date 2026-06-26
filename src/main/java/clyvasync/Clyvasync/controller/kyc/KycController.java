package clyvasync.Clyvasync.controller.kyc;

import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;
import clyvasync.Clyvasync.dto.request.KycBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.kyc.HostKycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/kyc")
@RequiredArgsConstructor
public class KycController {
    private final HostKycService hostKycService;
    @PostMapping("/profile")
    public ApiResponse<?> createProfile(
            @CurrentUserId Long userId,
            @Valid @RequestBody HostKycProfileRequest request) {
        return ApiResponse.success(hostKycService.createProfile(userId, request));
    }
    @PostMapping("/pre-upload")
    public ApiResponse<List<PreUploadResponse>> preUpload(
            @RequestBody KycBatchUploadRequest request) {
        System.out.println(request);

        return ApiResponse.success(hostKycService.prepareUploads(request.getProfileId(), request));
    }
}
