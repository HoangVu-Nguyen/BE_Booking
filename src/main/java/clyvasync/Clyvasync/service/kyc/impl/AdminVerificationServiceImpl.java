package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.response.HostPendingResponse;
import clyvasync.Clyvasync.enums.kyc.KycDocumentType;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.kyc.AdminVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminVerificationServiceImpl implements AdminVerificationService {
    private final HostKycProfileRepository kycProfileRepository;
    private final HostKycDocumentRepository hostKycDocumentRepository;
    @Override
    public List<HostPendingResponse> getPendingKycHosts() {
        List<HostKycProfile> pendingProfiles = kycProfileRepository.findByStatus(KycProfileStatus.PENDING_REVIEW);

        List<Long> profileIds = pendingProfiles.stream().map(HostKycProfile::getId).toList();
        List<HostKycDocument> allDocs = hostKycDocumentRepository.findAllById(profileIds);

        Map<Long, List<HostKycDocument>> docsByProfile = allDocs.stream()
                .collect(Collectors.groupingBy(HostKycDocument::getProfileId));

        return pendingProfiles.stream().map(profile -> {
            List<HostKycDocument> docs = docsByProfile.getOrDefault(profile.getId(), List.of());

            Integer aiScore = docs.stream()
                    .filter(d -> d.getDocumentType() == KycDocumentType.ID_FRONT)
                    .map(d -> d.getAiScore() != null ? d.getAiScore().intValue() : 0)
                    .findFirst()
                    .orElse(0);

            return HostPendingResponse.builder()
                    .id(String.valueOf(profile.getId()))
                    .name(profile.getLegalName())
                    .submittedAt(profile.getCreatedAt())
                    .aiConfidence(aiScore)
                    .build();
        }).collect(Collectors.toList());
    }
}
