package clyvasync.Clyvasync.controller.admin;

import clyvasync.Clyvasync.dto.request.RejectKycRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.HostKycDetailResponse;
import clyvasync.Clyvasync.dto.response.HostPendingResponse;
import clyvasync.Clyvasync.service.kyc.AdminVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/approvals")
@RequiredArgsConstructor
public class AdminApprovalController {

    private final AdminVerificationService adminVerificationService;

    @GetMapping("/kyc/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<HostPendingResponse>> getPendingKycList() {
        return ApiResponse.success( adminVerificationService.getPendingKycHosts());
    }
    @PostMapping("/kyc/{profileId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> approveKycProfile(@PathVariable Long profileId) {
        adminVerificationService.approveKyc(profileId);
        return ApiResponse.success();
    }

    @PostMapping("/kyc/{profileId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> rejectKycProfile(
            @PathVariable Long profileId,
            @RequestBody RejectKycRequest request) {

        adminVerificationService.rejectKyc(profileId, request.getReason());
        return ApiResponse.success();
    }
    @GetMapping("/kyc/detail/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HostKycDetailResponse> getKycDetail(@PathVariable Long profileId) {
        HostKycDetailResponse detail = adminVerificationService.getKycProfileDetail(profileId);
        return ApiResponse.success(detail);
    }
    @GetMapping("/kyc/count-pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> countPendingKyc() {
        return ApiResponse.success(adminVerificationService.countPendingKycProfiles());
    }
}