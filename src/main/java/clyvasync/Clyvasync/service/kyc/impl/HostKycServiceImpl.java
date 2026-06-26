package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;
import clyvasync.Clyvasync.enums.kyc.KycDocumentStatus;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.kyc.HostKycService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class HostKycServiceImpl implements HostKycService {
    private final HostKycProfileRepository profileRepository;
    private final HostKycDocumentRepository documentRepository;

    @Override
    public void createProfile(Long userId, HostKycProfileRequest request) {
        HostKycProfile profile = HostKycProfile.builder()
                .userId(userId)
                .legalName(request.getLegalName())
                .idCardNumber(request.getIdCardNumber())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankAccountOwner(request.getBankAccountOwner())
                .status(KycProfileStatus.PENDING_REVIEW)
                .build();
        profileRepository.save(profile);
    }

    @Override
    public void saveDocumentInfo(Long profileId, KycDocumentMeta meta, String fileUrl) {
        HostKycDocument doc = HostKycDocument.builder()
                .profileId(profileId)
                .documentType(meta.getDocumentType())
                .fileUrl(fileUrl)
                .status(KycDocumentStatus.PENDING)
                .build();
        documentRepository.save(doc);
    }
}
