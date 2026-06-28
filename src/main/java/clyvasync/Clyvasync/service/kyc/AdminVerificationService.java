package clyvasync.Clyvasync.service.kyc;

import clyvasync.Clyvasync.dto.response.HostKycDetailResponse;
import clyvasync.Clyvasync.dto.response.HostPendingResponse;

import java.util.List;

public interface AdminVerificationService {
    List<HostPendingResponse> getPendingKycHosts();
    HostKycDetailResponse getKycProfileDetail(Long profileId);
    void approveKyc(Long profileId);
    void rejectKyc(Long profileId, String reason);
}
