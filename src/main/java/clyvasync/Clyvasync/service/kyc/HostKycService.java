package clyvasync.Clyvasync.service.kyc;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;
import clyvasync.Clyvasync.dto.request.KycBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;

import java.util.List;

public interface HostKycService {
    Long createProfile(Long userId, HostKycProfileRequest request);

    void saveDocumentInfo(Long profileId, KycDocumentMeta meta, String fileUrl);

    List<PreUploadResponse> prepareUploads(Long profileId, KycBatchUploadRequest request);
    void confirmUpload(Long profileId, List<Long> documentIds);
}
