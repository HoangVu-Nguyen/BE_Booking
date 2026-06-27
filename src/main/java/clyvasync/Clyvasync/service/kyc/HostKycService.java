package clyvasync.Clyvasync.service.kyc;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import clyvasync.Clyvasync.dto.record.KycImagesResponse;
import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;
import clyvasync.Clyvasync.dto.request.KycBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.HostKycProfileResponse;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;

import java.util.List;
import java.util.Optional;

public interface HostKycService {
    Long createProfile(Long userId, HostKycProfileRequest request);

    void saveDocumentInfo(Long profileId, KycDocumentMeta meta, String fileUrl);

    List<PreUploadResponse> prepareUploads(Long profileId, KycBatchUploadRequest request);
    void confirmUpload(Long profileId, List<Long> documentIds);
    void processEkyc(Long profileId, List<String> imageUrls);
    KycImagesResponse getKycImagesForProfile(Long profileId);
    Optional<HostKycProfile> findByUserId(Long userId);
    HostKycProfileResponse getProfileResponse(Long userId);
    Long findIdByUserId(Long userId);
}
