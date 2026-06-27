package clyvasync.Clyvasync.controller.kyc;

import clyvasync.Clyvasync.dto.record.KycImagesResponse;
import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;
import clyvasync.Clyvasync.dto.request.KycBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.HostKycProfileResponse;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.kyc.HostKycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/confirm-upload")
    public ApiResponse<Void> confirmUpload(
          @CurrentUserId Long profileId,
            @RequestBody List<Long> documentIds) {


        hostKycService.confirmUpload(profileId, documentIds);

        return ApiResponse.success();
    }
    @GetMapping("/{profileId}/images")
    public ApiResponse<KycImagesResponse> getProfileImage(@PathVariable Long profileId) {
        return ApiResponse.success(hostKycService.getKycImagesForProfile(profileId));
    }
    @GetMapping("/my-profile")
    public ApiResponse<HostKycProfileResponse> getMyProfile(@CurrentUserId Long userId) {
        return ApiResponse.success(hostKycService.getProfileResponse(userId));
    }
    @GetMapping("/my-profile-status")
    public ApiResponse<Long> getMyProfileId(@CurrentUserId Long userId ) {
        return ApiResponse.success(hostKycService.findIdByUserId(userId));
    }
}
