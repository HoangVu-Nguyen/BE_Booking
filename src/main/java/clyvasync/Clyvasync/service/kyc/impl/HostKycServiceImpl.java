package clyvasync.Clyvasync.service.kyc.impl;

import clyvasync.Clyvasync.dto.detail.KycDocumentMeta;
import clyvasync.Clyvasync.dto.event.KycSubmittedEvent;
import clyvasync.Clyvasync.dto.record.KycImagesResponse;
import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.request.HostKycProfileRequest;
import clyvasync.Clyvasync.dto.request.KycBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.HostKycProfileResponse;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;
import clyvasync.Clyvasync.enums.kyc.KycDocumentStatus;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.kyc.HostKycMapper;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.kyc.HostKycDocumentRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.kyc.HostKycService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.utils.MediaUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
@Slf4j
public class HostKycServiceImpl implements HostKycService {
    private final HostKycProfileRepository profileRepository;
    private final HostKycDocumentRepository documentRepository;
    private final S3Service s3Service;
    private final MediaUtil mediaUtil;
    private final ApplicationEventPublisher eventPublisher;
    private final HostKycMapper hostKycMapper;

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
    @Override
    @Transactional
    public void confirmUpload(Long profileId, List<Long> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            throw new AppException(ResultCode.PROFILE_NOT_FOUND);
        }
        List<HostKycDocument> documents = documentRepository.findAllById(documentIds);

        if (documents.size() != documentIds.size()) {
            throw new AppException(ResultCode.DATA_NOT_FOUND);
        }

        for (HostKycDocument doc : documents) {
            if (!doc.getProfileId().equals(profileId)) {
                throw new AppException(ResultCode.ACCESS_DENIED);
            }

            if (doc.getStatus() != KycDocumentStatus.PENDING) {
                throw new AppException(ResultCode.INVALID_STATUS);
            }

            boolean isUploaded = s3Service.doesFileExist(doc.getFileUrl());
            if (!isUploaded) {
                log.error(">>>> [KYC] File chưa lên S3 nhưng bị gọi confirm: {}", doc.getFileUrl());
                throw new AppException(ResultCode.UPLOAD_FAILED);
            }
            doc.setStatus(KycDocumentStatus.SUBMITTED);
        }

        documentRepository.saveAll(documents);
        HostKycProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ResultCode.PROFILE_NOT_FOUND));

        profile.setStatus(KycProfileStatus.PENDING_REVIEW);
        profileRepository.save(profile);
        eventPublisher.publishEvent(new KycSubmittedEvent(profileId, documentIds));
    }
    @Override
    public void processEkyc(Long profileId, List<String> imageUrls) {
        log.info(">>>> [Mock eKYC] Đang gửi ảnh sang FPT.AI...");

        // Giả lập thời gian AI xử lý mất 3 giây
        try { Thread.sleep(3000); } catch (InterruptedException e) {}

        // Giả lập kết quả trả về random (80% thành công, 20% thất bại)
        boolean isSuccess = Math.random() > 0.2;

        if (isSuccess) {
            log.info(">>>> [Mock eKYC] Xác thực THÀNH CÔNG. Khớp 98%!");
            // Gọi repository update status = APPROVED
        } else {
            log.warn(">>>> [Mock eKYC] Xác thực THẤT BẠI. Ảnh mờ hoặc giả mạo.");
            // Gọi repository update status = REJECTED
        }
    }

    @Override
    public KycImagesResponse getKycImagesForProfile(Long profileId) {
        HostKycProfile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new AppException(ResultCode.PROFILE_NOT_FOUND));

        List<HostKycDocument> documents = documentRepository.findByProfileId(profileId);

        List<PresignedUrlResponse> presignedImages = documents.stream()
                .map(doc ->s3Service.generatePresignedUrl(doc.getFileUrl(),doc.getDocumentType()))
                .toList();

        return new KycImagesResponse(profileId, presignedImages);
    }

    @Override
    public Optional<HostKycProfile> findByUserId(Long userId) {
        return Optional.empty();
    }

    @Override
    public HostKycProfileResponse getProfileResponse(Long userId) {
        HostKycProfile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ResultCode.PROFILE_NOT_FOUND));
        return hostKycMapper.toHostKycProfileResponse(profile);
    }

    @Override
    public Long findIdByUserId(Long userId) {
        return profileRepository.findIdByUserId(userId).orElseThrow( () -> new AppException(ResultCode.PROFILE_NOT_FOUND));
    }
}
