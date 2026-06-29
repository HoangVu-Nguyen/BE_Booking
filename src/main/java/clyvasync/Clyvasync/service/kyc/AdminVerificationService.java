package clyvasync.Clyvasync.service.kyc;

import clyvasync.Clyvasync.dto.response.AdminHostResponse;
import clyvasync.Clyvasync.dto.response.HostKycDetailResponse;
import clyvasync.Clyvasync.dto.response.HostPendingResponse;
import clyvasync.Clyvasync.dto.response.PendingPropertyResponse;
import dto.request.ReviewPropertyRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminVerificationService {
    List<HostPendingResponse> getPendingKycHosts();
    HostKycDetailResponse getKycProfileDetail(Long profileId);
    void approveKyc(Long profileId);
    void rejectKyc(Long profileId, String reason);
    long countPendingKycProfiles();
    void submitPropertyReview(Long homestayId, ReviewPropertyRequest request);
    List<PendingPropertyResponse> getPendingProperties();
    Page<AdminHostResponse> getHostList(String keyword, Pageable pageable);
}
