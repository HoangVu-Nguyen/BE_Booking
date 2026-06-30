package clyvasync.Clyvasync.controller.admin;

import clyvasync.Clyvasync.dto.projection.RevenueProjection;
import clyvasync.Clyvasync.dto.request.RejectKycRequest;
import clyvasync.Clyvasync.dto.request.StatusUpdateRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.service.kyc.AdminVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    @GetMapping("/properties/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PendingPropertyResponse>> getPendingProperties() {
        return ApiResponse.success(adminVerificationService.getPendingProperties());
    }

    @PostMapping("/properties/{homestayId}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> submitReview(
            @PathVariable Long homestayId,
            @RequestBody dto.request.ReviewPropertyRequest request) {
        adminVerificationService.submitPropertyReview(homestayId, request);
        return ApiResponse.success();
    }
    @GetMapping("/hosts")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<AdminHostResponse>> getHostList(
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        System.out.println(pageable);

        PageResponse<AdminHostResponse> response = adminVerificationService.getHostList(keyword, pageable);
        return ApiResponse.success(response);
    }
    @GetMapping("/hosts/{hostId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HostDetailResponse> getHostDetail(@PathVariable Long hostId) {
        HostDetailResponse response = adminVerificationService.getHostDetail(hostId);
        System.out.println(response.toString());
        return ApiResponse.success(response);
    }
    @GetMapping("/hosts/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<HostOverviewMetricsResponse> getHostMetrics() {
        return ApiResponse.success(adminVerificationService.getHostOverviewMetrics());
    }
    @PostMapping("/properties/{homestayId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updatePropertyStatus(
            @PathVariable Long homestayId,
            @RequestBody StatusUpdateRequest request) {


        if (request.getStatus() == null || request.getReason() == null || request.getReason().isBlank()) {
            return ApiResponse.error(ResultCode.INVALID_INPUT, "Status and reason are required");
        }

        adminVerificationService.updatePropertyStatus(
                homestayId,
                request.getStatus(),
                request.getReason()
        );

        return ApiResponse.success(null);
    }
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<DashboardResponse> getDashboardSummary() {
        return ApiResponse.success(adminVerificationService.getDashboardSummary());
    }
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> getRevenue(@RequestParam String type) {
        System.out.println(adminVerificationService.getRevenueData(type).toString());
        return ApiResponse.success(adminVerificationService.getRevenueData(type));
    }
}