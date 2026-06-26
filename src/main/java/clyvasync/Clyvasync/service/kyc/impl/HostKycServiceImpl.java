package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;
import clyvasync.Clyvasync.dto.request.KycBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;
import clyvasync.Clyvasync.enums.kyc.KycDocumentStatus;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.kyc.HostKycService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.utils.MediaUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class HostKycServiceImpl implements HostKycService {
    private final HostKycProfileRepository profileRepository;
    private final HostKycDocumentRepository documentRepository;
    private final S3Service s3Service;
    private final MediaUtil mediaUtil;

    @Override
    @Transactional
    public Long createProfile(Long userId, HostKycProfileRequest request) {
        HostKycProfile profile = profileRepository.findByUserId(userId)
                .orElse(HostKycProfile.builder().userId(userId).build());

        profile.setLegalName(request.getLegalName());
        profile.setIdCardNumber(request.getIdCardNumber());
        profile.setBankName(request.getBankName());
        profile.setBankAccountNumber(request.getBankAccountNumber());
        profile.setBankAccountOwner(request.getBankAccountOwner());
        profile.setStatus(KycProfileStatus.PENDING_REVIEW);

        return profileRepository.save(profile).getId();
    }

    @Override
    @Transactional
    public void saveDocumentInfo(Long profileId, KycDocumentMeta meta, String fileUrl) {
        if (!profileRepository.existsById(profileId)) {
            throw new RuntimeException("Hồ sơ KYC không tồn tại, vui lòng điền thông tin trước!");
        }
        HostKycDocument doc = HostKycDocument.builder()
                .profileId(profileId)
                .documentType(meta.getDocumentType())
                .fileUrl(fileUrl)
                .status(KycDocumentStatus.PENDING)
                .build();
        documentRepository.save(doc);
    }

    @Override
    @Transactional
    public List<PreUploadResponse> prepareUploads(Long profileId, KycBatchUploadRequest request) {
        if (!profileRepository.existsById(profileId)) {
            throw new AppException(ResultCode.PROFILE_NOT_FOUND);
        }

        List<HostKycDocument> documents = request.getItems().stream()
                .map(item -> {

                    String objectKey = mediaUtil.generateObjectKey(profileId, item);

                    return HostKycDocument.builder()
                            .profileId(profileId)
                            .documentType(item.getDocumentType())
                            .fileName(item.getFileName())
                            .status(KycDocumentStatus.PENDING)
                            .fileUrl(objectKey)
                            .build();

                })
                .toList();

        List<HostKycDocument> savedDocs = documentRepository.saveAll(documents);

        return IntStream.range(0, savedDocs.size())
                .mapToObj(i -> {

                    HostKycDocument doc = savedDocs.get(i);

                    KycDocumentMeta meta = request.getItems().get(i);

                    String uploadUrl = s3Service.generatePresignedPutUrl(
                            doc.getFileUrl(),
                            meta.getContentType(),
                            meta.getFileSize());

                    return new PreUploadResponse(
                            doc.getId(),
                            meta.getFileName(),
                            doc.getFileUrl(),
                            uploadUrl);

                })
                .toList();
    }
}
