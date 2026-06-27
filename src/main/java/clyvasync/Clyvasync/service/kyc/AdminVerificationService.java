package clyvasync.Clyvasync.service.kyc;

import clyvasync.Clyvasync.dto.response.HostPendingResponse;

import java.util.List;

public interface AdminVerificationService {
    List<HostPendingResponse> getPendingKycHosts();
}
