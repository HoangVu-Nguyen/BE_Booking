package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.detail.ActivityDto;
import clyvasync.Clyvasync.dto.detail.RevenueData;
import clyvasync.Clyvasync.dto.event.HomestayStatusChangedEvent;
import clyvasync.Clyvasync.dto.event.KycProcessedEvent;
import clyvasync.Clyvasync.dto.event.PropertyVerificationEvent;
import clyvasync.Clyvasync.dto.projection.*;
import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.homestay.DocumentStatus;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.homestay.PropertyDocumentType;
import clyvasync.Clyvasync.enums.kyc.KycDocumentStatus;
import clyvasync.Clyvasync.enums.kyc.KycDocumentType;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.enums.user.UserStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.homestay.entity.*;
import clyvasync.Clyvasync.modules.host.entity.HostAuditLog;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.modules.wallet.entity.HostWallet;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.repository.homestay.CategoryRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayImageRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayStatusHistoryRepository;
import clyvasync.Clyvasync.repository.homestay.LocationRepository;
import clyvasync.Clyvasync.repository.host.HostAuditLogRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.repository.wallet.HostWalletRepository;
import clyvasync.Clyvasync.repository.wallet.WalletTransactionRepository;
import clyvasync.Clyvasync.service.auth.RoleService;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayImageService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.kyc.AdminVerificationService;
import clyvasync.Clyvasync.service.media.IUserPhotoService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.utils.MediaUtil;
import dto.request.ReviewPropertyRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminVerificationServiceImpl implements AdminVerificationService {
    private final HostKycProfileRepository kycProfileRepository;
    private final HostKycDocumentRepository hostKycDocumentRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final RoleService roleService;
    private final ApplicationEventPublisher eventPublisher;
    private final clyvasync.Clyvasync.repository.homestay.HomestayRepository homestayRepository;
    private final clyvasync.Clyvasync.repository.homestay.HomestayDocumentRepository documentRepository;
    private final UserService userService;
    private final HostWalletRepository walletRepository;
    private final IUserPhotoService userPhotoService;
    private final HomestayService homestayService;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final HostWalletService walletService;
    private final HomestayImageRepository homestayImageRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final CategoryRepository categoryRepository;
    private final LocationRepository locationRepository;
    private final MediaUtil mediaUtil;
    private final HostKycDocumentRepository kycDocumentRepository;
    private final HomestayStatusHistoryRepository homestayStatusHistoryRepository;
    private final HostAuditLogRepository hostAuditLogRepository;

    @Override
    public List<HostPendingResponse> getPendingKycHosts() {
        List<HostKycProfile> pendingProfiles = kycProfileRepository.findByStatus(KycProfileStatus.PENDING_REVIEW);

        if (pendingProfiles.isEmpty()) {
            return List.of();
        }

        List<Long> profileIds = pendingProfiles.stream()
                .map(HostKycProfile::getId)
                .toList();

        List<HostKycDocument> frontIdDocs = hostKycDocumentRepository.findByProfileIdInAndDocumentType(
                profileIds, KycDocumentType.ID_FRONT
        );

        Map<Long, HostKycDocument> docMap = frontIdDocs.stream()
                .collect(Collectors.toMap(HostKycDocument::getProfileId, doc -> doc, (d1, d2) -> d1));

        return pendingProfiles.stream().map(profile -> {
            HostKycDocument doc = docMap.get(profile.getId());
            Integer aiScore = (doc != null && doc.getAiScore() != null) ? doc.getAiScore().intValue() : 0;

            return HostPendingResponse.builder()
                    .profileId(profile.getId())
                    .name(profile.getLegalName())
                    .aiConfidence(aiScore)
                    .submittedAt(profile.getCreatedAt())
                    .status(profile.getStatus())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public HostKycDetailResponse getKycProfileDetail(Long profileId) {
        HostKycProfile profile = kycProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ResultCode.PROFILE_NOT_FOUND));

        User user = userRepository.findById(profile.getUserId())
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        List<HostKycDocument> documents = hostKycDocumentRepository.findByProfileId(profileId);

        String frontImg = null;
        String backImg = null;
        String selfieImg = null;
        Double aiConfidence = 0.0;
        String ocrResult = null;

        for (HostKycDocument doc : documents) {
            String fileKey = doc.getFileUrl();
            String safeUrl = null;

            if (fileKey != null && !fileKey.trim().isEmpty()) {
                PresignedUrlResponse tempUrl = s3Service.generatePresignedUrl(fileKey, doc.getDocumentType());
                if (tempUrl != null) {
                    safeUrl = tempUrl.url();
                }
            }

            if (doc.getDocumentType() == KycDocumentType.ID_FRONT) {
                frontImg = safeUrl;
                aiConfidence = doc.getAiScore() != null ? doc.getAiScore().doubleValue() : 0.0;
                ocrResult = doc.getOcrData();
            } else if (doc.getDocumentType() == KycDocumentType.ID_BACK) {
                backImg = safeUrl;
            } else if (doc.getDocumentType() == KycDocumentType.SELFIE) {
                selfieImg = safeUrl;
            }
        }

        return HostKycDetailResponse.builder()
                .profileId(profile.getId())
                .name(profile.getLegalName())
                .email(user.getEmail())
                .phone(user.getPhoneNumber())
                .citizenId(profile.getIdCardNumber())
                .issueDate(profile.getIdCardIssuedDate())
                .issueBy(profile.getIdCardIssuedBy())
                .frontImage(frontImg)
                .backImage(backImg)
                .selfie(selfieImg)
                .aiScore(aiConfidence)
                .ocrData(ocrResult)
                .build();
    }

    @Override
    public void approveKyc(Long profileId) {
        HostKycProfile profile = kycProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ResultCode.PROFILE_NOT_FOUND));
        profile.setStatus(KycProfileStatus.APPROVED);
        profile.setRejectionReason(null);
        kycProfileRepository.save(profile);
        roleService.upgradeToHost(profile.getUserId());
        eventPublisher.publishEvent(new KycProcessedEvent(
                profile.getUserId(),
                KycProfileStatus.APPROVED,
                "Chúc mừng! Hồ sơ KYC của bạn đã được phê duyệt."
        ));
    }

    @Override
    public void rejectKyc(Long profileId, String reason) {
        HostKycProfile profile = kycProfileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ResultCode.PROFILE_NOT_FOUND));

        profile.setStatus(KycProfileStatus.REJECTED);
        profile.setRejectionReason(reason);
        kycProfileRepository.save(profile);

        eventPublisher.publishEvent(new KycProcessedEvent(
                profile.getUserId(),
                KycProfileStatus.REJECTED,
                reason
        ));

    }

    @Override
    public long countPendingKycProfiles() {
        return kycProfileRepository.countByStatus(KycProfileStatus.PENDING_REVIEW);
    }

    @Override
    @Transactional
    public void submitPropertyReview(Long homestayId, ReviewPropertyRequest request) {

        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        List<Long> documentIds = request.getDocuments().stream()
                .map(ReviewPropertyRequest.DocumentReviewItem::getDocumentId)
                .collect(Collectors.toList());

        List<HomestayDocument> documents = documentRepository.findAllById(documentIds);

        Map<Long, HomestayDocument> documentMap = documents.stream()
                .collect(Collectors.toMap(HomestayDocument::getId, doc -> doc));

        boolean hasAnyRejection = false;

        for (ReviewPropertyRequest.DocumentReviewItem item : request.getDocuments()) {
            HomestayDocument doc = documentMap.get(item.getDocumentId());

            if (doc == null) {
                throw new AppException(ResultCode.DOCUMENT_NOT_FOUND);
            }

            if (!doc.getHomestayId().equals(homestayId)) {
                throw new AppException(ResultCode.DOCUMENT_ACCESS_DENIED);
            }
            doc.setStatus(item.getStatus());
            if (DocumentStatus.REJECTED.equals(item.getStatus())) {
                doc.setRejectionReason(item.getRejectReason());
                hasAnyRejection = true;
            } else {
                doc.setRejectionReason(null);
            }
        }
        documentRepository.saveAll(documents);

        if (hasAnyRejection) {
            homestay.setStatus(HomestayStatus.REJECTED);
        } else {
            homestay.setStatus(HomestayStatus.APPROVED);
        }

        homestayRepository.save(homestay);

        eventPublisher.publishEvent(PropertyVerificationEvent.builder()
                .userId(homestay.getOwnerId())
                .homestayId(homestay.getId())
                .homestayName(homestay.getName())
                .status(homestay.getStatus())
                .build());
    }

    @Override
    @Transactional
    public List<PendingPropertyResponse> getPendingProperties() {
        List<HomestayStatus> draftStatus = List.of(HomestayStatus.DRAFT, HomestayStatus.PENDING_VERIFICATION,HomestayStatus.SUSPENDED);
        List<Homestay> draftHomestays = homestayRepository.findByStatusIn(draftStatus);
        if (draftHomestays.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> homestayIds = draftHomestays.stream()
                .map(Homestay::getId)
                .toList();
        List<HomestayDocument> allDocuments = documentRepository.findByHomestayIdIn(homestayIds);
        Map<Long, List<HomestayDocument>> docsByHomestayId = allDocuments.stream()
                .collect(Collectors.groupingBy(HomestayDocument::getHomestayId));
        List<Long> userIds = draftHomestays.stream().map(Homestay::getOwnerId).distinct().toList();
        Map<Long, OwnerResponse> ownerResponseMap = userService.getOwnerInfos(userIds);
        return draftHomestays.stream()
                .filter(homestay -> docsByHomestayId.containsKey(homestay.getId()))
                .map(homestay -> {
                    List<HomestayDocument> docs = docsByHomestayId.get(homestay.getId());

                    List<PendingPropertyResponse.DocumentDto> docDtos = docs.stream()
                            .map(doc -> PendingPropertyResponse.DocumentDto.builder()
                                    .id(doc.getId())
                                    .name(doc.getDocumentType().name())
                                    .url(s3Service.generatePresignedUrl(
                                            doc.getFileUrl(),
                                            PropertyDocumentType.valueOf(doc.getDocumentType().name())
                                    ).url())
                                    .status(doc.getStatus())
                                    .build())
                            .collect(Collectors.toList());

                    return PendingPropertyResponse.builder()
                            .id(String.valueOf(homestay.getId()))
                            .homestayName(homestay.getName())
                            .hostName(ownerResponseMap.get(homestay.getOwnerId()).getFullName())
                            .documents(docDtos)
                            .submittedAt(homestay.getUpdatedAt())
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AdminHostResponse> getHostList(String keyword, Pageable pageable) {
        Page<User> hostPage = userRepository.findByRole(RoleName.HOST, keyword, pageable);
        log.info(">>>> [Debug] Page number: {}, Total elements: {}", hostPage.getNumber(), hostPage.getTotalElements());

        if (hostPage.isEmpty()) {
            return PageResponse.<AdminHostResponse>builder()
                    .content(List.of())
                    .totalElements(0)
                    .totalPages(0)
                    .number(pageable.getPageNumber())
                    .size(pageable.getPageSize())
                    .last(true)
                    .first(true)
                    .build();
        }

        List<Long> hostIds = hostPage.getContent().stream().map(User::getId).toList();

        var propertyMap = homestayRepository.getPropertyStatsByOwners(hostIds, List.of(HomestayStatus.DRAFT, HomestayStatus.PENDING_VERIFICATION, HomestayStatus.APPROVED))
                .stream().collect(Collectors.toMap(HostPropertyStatsProjection::getOwnerId, p -> p));
        var kycMap = kycProfileRepository.getKycStatsByUsers(hostIds)
                .stream().collect(Collectors.toMap(HostKycStatsProjection::getUserId, k -> k));
        var walletMap = walletRepository.getWalletBalancesByOwners(hostIds)
                .stream().collect(Collectors.toMap(HostWalletProjection::getOwnerId, HostWalletProjection::getBalance));
        var financialMap = bookingRepository.sumFinancialMetricsByOwners(hostIds)
                .stream().collect(Collectors.toMap(HostFinancialProjection::getOwnerId, f -> f));

        var photoMap = userPhotoService.getAvatarsMapByIds(hostIds);


        List<AdminHostResponse> dtoList = hostPage.getContent().stream().map(user -> {
            Long hId = user.getId();
            var pStats = propertyMap.get(hId);
            var kStats = kycMap.get(hId);
            var fStats = financialMap.get(hId);

            long totalBookingsAllStatus = (fStats != null && fStats.getTotalBookingsAllStatus() != null) ? fStats.getTotalBookingsAllStatus() : 0;
            long cancelledBookings = (fStats != null && fStats.getCancelledBookings() != null) ? fStats.getCancelledBookings() : 0;
            long completedBookings = (fStats != null && fStats.getCompletedBookings() != null) ? fStats.getCompletedBookings() : 0;

            double cancellationRate = 0.0;
            if (totalBookingsAllStatus > 0) {
                cancellationRate = ((double) cancelledBookings / totalBookingsAllStatus) * 100.0;
                cancellationRate = Math.round(cancellationRate * 10.0) / 10.0;
            }

            String finalStatus;

            if (user.getStatus() == UserStatus.SUSPENDED) {
                finalStatus = "SUSPENDED";
            }
            else if (kStats == null || "MISSING".equals(kStats.getKycStatus())) {
                finalStatus = "MISSING";
            }
            else if ("PENDING_REVIEW".equals(kStats.getKycStatus())) {
                finalStatus = "PENDING_REVIEW";
            }
            else if ("REJECTED".equals(kStats.getKycStatus())) {
                finalStatus = "REJECTED";
            }
            else {
                finalStatus = "ACTIVE";
            }

            return AdminHostResponse.builder()
                    .id(String.valueOf(hId))
                    .joinDate(user.getCreatedAt())
                    .status(finalStatus)
                    .user(UserHeaderResponse.builder()
                            .username(user.getFullName())
                            .email(user.getEmail())
                            .phoneNumber(user.getPhoneNumber())
                            .photoUrl(photoMap.get(hId))
                            .build())
                    .verification(AdminHostResponse.VerificationInfo.builder()
                            .identityStatus(kStats != null ? kStats.getKycStatus() : "MISSING")
                            .bankStatus("LINKED")
                            .build())
                    .metrics(AdminHostResponse.HostMetrics.builder()
                            .totalProperties(pStats != null ? pStats.getTotalProperties() : 0)
                            .pendingProperties((pStats != null ? pStats.getPendingProperties() : 0) + (kStats != null ? kStats.getPendingKycDocs() : 0))
                            .walletBalance(walletMap.getOrDefault(hId, BigDecimal.ZERO))
                            .totalRevenue(fStats != null ? fStats.getGmv() : BigDecimal.ZERO)
                            .totalBookings((int) completedBookings)
                            .cancellationRate(cancellationRate)
                            .averageResponseTime("100")
                            .build())
                    .build();
        }).collect(Collectors.toList());

        return PageResponse.<AdminHostResponse>builder()
                .content(dtoList)
                .totalElements(hostPage.getTotalElements())
                .totalPages(hostPage.getTotalPages())
                .number(hostPage.getNumber())
                .size(hostPage.getSize())
                .last(hostPage.isLast())
                .first(hostPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HostDetailResponse getHostDetail(Long hostId) {
        User user = userRepository.findById(hostId)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));
        UserHeaderResponse userHeaderResponse = userService.getHeaderInfo(hostId);

        HostKycProfile kyc = kycProfileRepository.findByUserId(hostId).orElse(null);
        HostWallet wallet = walletRepository.findByOwnerId(hostId).orElse(null);

        List<Homestay> homestays = homestayRepository.findAllByOwnerId(hostId);
        List<Long> homestayIds = homestays.stream().map(Homestay::getId).toList();

        final Map<Long, HomestayFinancialProjection> financeMap = new HashMap<>();
        final Map<Long, String> imageMap = new HashMap<>();
        final Map<Integer, String> categoryMap = new HashMap<>();
        final Map<Integer, String> locationMap = new HashMap<>();
        final List<WalletTransaction> transactions = new ArrayList<>();
        String frontUrl = "";
        String backUrl = "";
        if (kyc != null) {
            List<HostKycDocument> hostKycDocuments = kycDocumentRepository.findByProfileId(kyc.getId());
            String frontObjectKey = hostKycDocuments.stream()
                    .filter(d -> KycDocumentType.ID_FRONT.equals(d.getDocumentType()))
                    .map(HostKycDocument::getFileUrl)
                    .findFirst().orElse("");

            String backObjectKey = hostKycDocuments.stream()
                    .filter(d -> KycDocumentType.ID_BACK.equals(d.getDocumentType()))
                    .map(HostKycDocument::getFileUrl)
                    .findFirst().orElse("");

            if (!frontObjectKey.isEmpty()) {
                frontUrl = s3Service.generatePresignedUrl(frontObjectKey, KycDocumentType.ID_FRONT).url();
            }
            if (!backObjectKey.isEmpty()) {
                backUrl = s3Service.generatePresignedUrl(backObjectKey, KycDocumentType.ID_BACK).url();
            }
        }

        if (!homestayIds.isEmpty()) {
            List<HomestayFinancialProjection> finances = bookingRepository.getFinancialStatsByHomestayIds(homestayIds);
            financeMap.putAll(finances.stream().collect(Collectors.toMap(
                    HomestayFinancialProjection::getHomestayId,
                    f -> f,
                    (existing, replacement) -> replacement
            )));

            List<HomestayImageProjection> images = homestayImageRepository.findPrimaryImagesByHomestayIds(homestayIds);
            imageMap.putAll(images.stream().collect(Collectors.toMap(
                    HomestayImageProjection::getHomestayId,
                    HomestayImageProjection::getImageUrl,
                    (existing, replacement) -> existing
            )));

            List<Integer> categoryIds = homestays.stream().map(Homestay::getCategoryId).filter(Objects::nonNull).distinct().toList();
            List<Integer> locationIds = homestays.stream().map(Homestay::getLocationId).filter(Objects::nonNull).distinct().toList();

            if (!categoryIds.isEmpty()) {
                categoryMap.putAll(categoryRepository.findAllById(categoryIds).stream()
                        .collect(Collectors.toMap(Category::getId, Category::getName)));
            }
            if (!locationIds.isEmpty()) {
                locationMap.putAll(locationRepository.findAllById(locationIds).stream()
                        .collect(Collectors.toMap(Location::getId, Location::getCityName)));
            }
        }

        if (wallet != null) {
            transactions.addAll(walletTransactionRepository.findTop10ByWalletIdOrderByCreatedAtDesc(wallet.getId()));
        }
        long hostValidBookings = 0;
        long hostCompletedBookings = 0;
        long hostTotalBookingsAllStatus = 0;
        long hostCancelledBookings = 0;
        BigDecimal hostTotalRevenue = BigDecimal.ZERO;

        double hostTotalRating = 0;
        int hostTotalReviews = 0;
        int ratedHomestayCount = 0;

        for (Homestay h : homestays) {
            HomestayFinancialProjection stats = financeMap.get(h.getId());
            if (stats != null) {
                hostCompletedBookings += (stats.getCompletedBookings() != null ? stats.getCompletedBookings() : 0);
                hostTotalBookingsAllStatus += (stats.getTotalBookingsAllStatus() != null ? stats.getTotalBookingsAllStatus() : 0);
                hostCancelledBookings += (stats.getCancelledBookings() != null ? stats.getCancelledBookings() : 0);
                hostTotalRevenue = hostTotalRevenue.add(stats.getTotalRevenue() != null ? stats.getTotalRevenue() : BigDecimal.ZERO);
                long totalAll = stats.getTotalBookingsAllStatus() != null ? stats.getTotalBookingsAllStatus() : 0;
                long cancelled = stats.getCancelledBookings() != null ? stats.getCancelledBookings() : 0;
                hostTotalBookingsAllStatus += totalAll;
                hostCancelledBookings += cancelled;
                hostValidBookings += (totalAll - cancelled);
            }

            if (h.getAverageRating() != null && h.getAverageRating().doubleValue() > 0) {
                hostTotalRating += h.getAverageRating().doubleValue();
                ratedHomestayCount++;
            }
            if (h.getReviewCount() != null) {
                hostTotalReviews += h.getReviewCount();
            }
        }

        double avgHostRating = ratedHomestayCount > 0 ? (hostTotalRating / ratedHomestayCount) : 0.0;
        double cancellationRate = hostTotalBookingsAllStatus > 0
                ? ((double) hostCancelledBookings / hostTotalBookingsAllStatus) * 100.0
                : 0.0;
        cancellationRate = Math.round(cancellationRate * 10.0) / 10.0;


        List<HostDetailResponse.PropertyDto> propertyDtos = homestays.stream().map(h -> {
            HomestayFinancialProjection stats = financeMap.get(h.getId());

            String categoryName = categoryMap.getOrDefault(h.getCategoryId(), "Chưa phân loại");
            String cityName = locationMap.getOrDefault(h.getLocationId(), "Chưa cập nhật");
            String fullLocation = h.getAddressDetail() + ", " + cityName;

            return HostDetailResponse.PropertyDto.builder()
                    .id(String.valueOf(h.getId()))
                    .name(h.getName())
                    .type(categoryName)
                    .location(fullLocation)
                    .image(mediaUtil.toCdnUrl(imageMap.get(h.getId())))
                    .status(h.getStatus().name())
                    .metrics(HostDetailResponse.PropertyMetricsDto.builder()
                            .bookings(stats != null ? (int) (stats.getTotalBookingsAllStatus() - stats.getCancelledBookings()) : 0)
                            .revenue(stats != null && stats.getTotalRevenue() != null ? stats.getTotalRevenue() : BigDecimal.ZERO)
                            .rating(h.getAverageRating() != null ? h.getAverageRating().doubleValue() : 0.0)
                            .build())
                    .build();
        }).collect(Collectors.toList());

        List<HostDetailResponse.AuditLogDto> auditLogs = transactions.stream().map(t ->
                HostDetailResponse.AuditLogDto.builder()
                        .time(t.getCreatedAt().toString())
                        .action(t.getTransactionType().name())
                        .desc(t.getDescription())
                        .status(t.getStatus().name())
                        .build()
        ).collect(Collectors.toList());

        return HostDetailResponse.builder()
                .host(HostDetailResponse.HostInfo.builder()
                        .id(String.valueOf(user.getId()))
                        .joinDate(user.getCreatedAt().toString())
                        .status(user.isActive() ? "ACTIVE" : "SUSPENDED")
                        .walletBalance(wallet != null ? wallet.getAvailableBalance() : BigDecimal.ZERO)
                        .totalRevenue(hostTotalRevenue)
                        .user(UserHeaderResponse.builder()
                                .username(user.getFullName())
                                .email(user.getEmail())
                                .phoneNumber(user.getPhoneNumber())
                                .photoUrl(userHeaderResponse.getPhotoUrl())
                                .build())
                        .kyc(HostDetailResponse.KycDto.builder()
                        .identity(kyc != null ? kyc.getStatus().name() : "MISSING")
                        .idNumber(kyc != null ? kyc.getIdCardNumber() : "N/A")
                        .frontImageUrl(frontUrl)
                        .backImageUrl(backUrl)
                        .bankInfo(HostDetailResponse.BankInfoDto.builder()
                                .bankName(kyc != null ? kyc.getBankName() : "N/A")
                                .accountNo(kyc != null ? kyc.getBankAccountNumber() : "N/A")
                                .ownerName(kyc != null ? kyc.getBankName() : "N/A")
                                .build())
                        .build())
                        .metrics(HostDetailResponse.MetricsDto.builder()
                                .totalBookings((int) hostValidBookings)
                                .cancellationRate(cancellationRate)
                                .responseRate(100.0)
                                .avgRating(Math.round(avgHostRating * 100.0) / 100.0)
                                .reviewsCount(hostTotalReviews)
                                .build())
                        .build())
                .properties(propertyDtos)
                .auditLogs(auditLogs)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public HostOverviewMetricsResponse getHostOverviewMetrics() {
        long totalHosts = userRepository.countByRoleName(RoleName.HOST);
        long pendingKyc = kycProfileRepository.countByStatus(KycProfileStatus.PENDING_REVIEW);
        long totalProperties = homestayRepository.count();
        long suspendedHosts = userRepository.countByRoleNameAndIsActive(RoleName.HOST, false);
        return HostOverviewMetricsResponse.builder()
                .totalHosts(totalHosts)
                .pendingKycHosts(pendingKyc)
                .totalProperties(totalProperties)
                .suspendedHosts(suspendedHosts)
                .build();

    }

    @Override
    @Transactional()
    public void updatePropertyStatus(Long homestayId, String newStatusStr, String reason) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));
        HomestayStatus oldStatus = homestay.getStatus();
        HomestayStatus newStatus = HomestayStatus.valueOf(newStatusStr);
        homestay.setStatus(newStatus);
        homestayRepository.save(homestay);
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        HomestayStatusHistory history = HomestayStatusHistory.builder()
                .homestayId(homestayId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .changedBy(adminEmail)
                .createdAt(LocalDateTime.now())
                .build();

        homestayStatusHistoryRepository.save(history);
        eventPublisher.publishEvent(HomestayStatusChangedEvent.builder()
                .userId(homestay.getOwnerId())
                .homestayId(homestayId)
                .homestayName(homestay.getName())
                .newStatus(newStatus)
                .reason(reason)
                .build());
    }

    @Override
    public DashboardResponse getDashboardSummary() {
        // 1. Lấy dữ liệu với mặc định là 0.0 để tránh NPE
        Double gmvToday = bookingRepository.sumTotalPriceByDate(LocalDate.now());
        double safeGmvToday = (gmvToday != null) ? gmvToday : 0.0;

        Double gmvYesterday = bookingRepository.sumTotalPriceByDate(LocalDate.now().minusDays(1));
        double safeGmvYesterday = (gmvYesterday != null) ? gmvYesterday : 0.0;

        double growth = 0.0;
        if (safeGmvYesterday > 0) {
            growth = ((safeGmvToday - safeGmvYesterday) / safeGmvYesterday) * 100;
        } else if (safeGmvToday > 0) {
            growth = 100.0;
        }
        List<ActivityProjection> activityProjections = hostAuditLogRepository.getRecentActivities();
        List<ActivityDto> recentActivities = activityProjections.stream().map(p ->
                ActivityDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .time(p.getTime())
                        .type(p.getType())
                        .status(p.getStatus())
                        .build()
        ).toList();

        return DashboardResponse.builder()
                .gmvToday(safeGmvToday)
                .gmvGrowthPercentage(Math.round(growth * 10) / 10.0)
                .newBookings(bookingRepository.count())
                .pendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING))
                .newHosts(userRepository.countByRoleName(RoleName.HOST))
                .pendingKycCount(kycProfileRepository.countByStatus(KycProfileStatus.PENDING_REVIEW))
                .revenueChart(prepareRevenueChartData())
                .recentActivities(recentActivities)
                .build();
    }

    @Override
    public RevenueResponse getRevenueData(String type) {
        LocalDateTime startDate = switch (type) {
            case "WEEK" -> LocalDateTime.now().minusWeeks(12);
            case "MONTH" -> LocalDateTime.now().minusMonths(6);
            case "QUARTER" -> LocalDateTime.now().minusMonths(24);
            case "YEAR" -> LocalDateTime.now().minusYears(5);
            default -> LocalDateTime.now().minusMonths(6);
        };
        List<RevenueProjection> projections = bookingRepository.getRevenueReport(type, startDate);
        return RevenueResponse.builder()
                .labels(projections.stream().map(RevenueProjection::getTimeLabel).toList())
                .revenue(projections.stream().map(RevenueProjection::getRevenue).toList())
                .gmv(projections.stream().map(RevenueProjection::getGmv).toList())
                .build();

    }

    @Override
    @Transactional
    public void suspendHost(Long hostId, String reason, Integer days) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        host.setStatus(UserStatus.SUSPENDED);

        if (days != null && days > 0) {
            host.setSuspendedUntil(LocalDateTime.now().plusDays(days));
        } else {
            host.setSuspendedUntil(null);
        }

        userRepository.save(host);

        hostAuditLogRepository.save(HostAuditLog.builder()
                .hostId(hostId)
                .action(UserStatus.SUSPENDED)
                .reason(reason + " (Thời hạn: " + days + " ngày)")
                .createdAt(LocalDateTime.now())
                .build());
    }

    private List<RevenueData> prepareRevenueChartData() {
        List<RevenueProjection> projections = bookingRepository.getRevenueLast7Days();
        LocalDate today = LocalDate.now();

        return projections.stream().map(p -> {
            boolean isToday = p.getDay().equals(today.format(DateTimeFormatter.ofPattern("E", new Locale("vi"))));

            return RevenueData.builder()
                    .day(p.getDay())
                    .value(p.getValue())
                    .label(formatCurrency(p.getValue()))
                    .isToday(isToday)
                    .build();
        }).collect(Collectors.toList());
    }
    private String formatCurrency(Double value) {
        if (value == null) return "0";

        if (value >= 1_000_000_000) {
            return String.format("%.1fB", value / 1_000_000_000);
        } else if (value >= 1_000_000) {
            return String.format("%.0fM", value / 1_000_000);
        } else if (value >= 1_000) {
            return String.format("%.0fK", value / 1_000);
        }

        return String.format("%.0f", value);
    }
}
