package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.event.KycProcessedEvent;
import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.response.HostKycDetailResponse;
import clyvasync.Clyvasync.dto.response.HostPendingResponse;
import clyvasync.Clyvasync.enums.kyc.KycDocumentType;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.auth.RoleService;
import clyvasync.Clyvasync.service.kyc.AdminVerificationService;
import clyvasync.Clyvasync.service.media.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

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
}
