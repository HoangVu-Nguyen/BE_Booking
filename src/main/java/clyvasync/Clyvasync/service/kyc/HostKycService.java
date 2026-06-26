package clyvasync.Clyvasync.service.kyc;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;

public interface HostKycService {
    void createProfile(Long userId, HostKycProfileRequest request);
    void saveDocumentInfo(Long profileId, KycDocumentMeta meta, String fileUrl);
}
