package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.event.KycProcessedEvent;
import clyvasync.Clyvasync.dto.event.PropertyVerificationEvent;
import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.response.HostKycDetailResponse;
import clyvasync.Clyvasync.dto.response.HostPendingResponse;
import clyvasync.Clyvasync.dto.response.OwnerResponse;
import clyvasync.Clyvasync.dto.response.PendingPropertyResponse;
import clyvasync.Clyvasync.enums.homestay.DocumentStatus;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.homestay.PropertyDocumentType;
import clyvasync.Clyvasync.enums.kyc.KycDocumentType;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.auth.RoleService;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.kyc.AdminVerificationService;
import clyvasync.Clyvasync.service.media.S3Service;
import dto.request.ReviewPropertyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVerificationServiceImpl implements AdminVerificationService {
    private final HostKycProfileRepository kycProfileRepository;
    private final HostKycDocumentRepository hostKycDocumentRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;
    private final RoleService roleService;
    private final ApplicationEventPublisher eventPublisher;
    private final clyvasync.Clyvasync.repository.homestay.HomestayRepository homestayRepository;
    private final clyvasync.Clyvasync.repository.homestay.HomestayDocumentRepository documentRepository;
    private final UserService  userService;
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
        List<HomestayStatus> draftStatus = List.of(HomestayStatus.DRAFT, HomestayStatus.PENDING_VERIFICATION);
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
                            .id("PRP-" + homestay.getId())
                            .homestayName(homestay.getName())
                            .hostName(ownerResponseMap.get(homestay.getOwnerId()).getFullName())
                            .documents(docDtos)
                            .submittedAt(homestay.getUpdatedAt())
                            .build();
                }).collect(Collectors.toList());
    }

}
