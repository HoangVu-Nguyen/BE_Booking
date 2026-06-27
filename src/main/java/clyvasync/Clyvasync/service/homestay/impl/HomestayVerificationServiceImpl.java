package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.detail.HomestayDocumentMeta;
import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.request.DocumentUploadRequest;
import clyvasync.Clyvasync.dto.request.HomestayBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.FptOcrResponse;
import clyvasync.Clyvasync.dto.response.HomestayDocumentResponse;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;
import clyvasync.Clyvasync.enums.homestay.DocumentStatus;
import clyvasync.Clyvasync.enums.homestay.PropertyDocumentType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayDocument;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import clyvasync.Clyvasync.repository.homestay.HomestayDocumentRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.repository.kyc.HostKycProfileRepository;
import clyvasync.Clyvasync.service.TesseractOcrService;
import clyvasync.Clyvasync.service.homestay.HomestayVerificationService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.utils.MediaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomestayVerificationServiceImpl implements HomestayVerificationService {
    private final HomestayRepository homestayRepository;
    private final S3Service s3Service;
    private final MediaUtil mediaUtil;
    private final HomestayDocumentRepository documentRepository;
    private final HostKycProfileRepository hostKycRepository;
    private final TesseractOcrService tesseractService;

    @Override
    public HomestayDocumentResponse addDocument(Long homestayId, Long hostId, DocumentUploadRequest request) {
        return null;
    }

    @Override
    public List<HomestayDocumentResponse> getDocuments(Long homestayId, Long hostId) {
        if (!homestayRepository.existsByIdAndOwnerId(homestayId, hostId)) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }

        List<HomestayDocument> documents = documentRepository.findByHomestayIdOrderByCreatedAtDesc(homestayId);

        return documents.stream().map(doc -> {
            String viewUrl = "";
            try {
                PresignedUrlResponse s3Response = s3Service.generatePresignedUrl(doc.getFileUrl(), doc.getDocumentType());

                viewUrl = s3Response.url();
            } catch (Exception e) {
                log.error("Không thể tạo link xem ảnh cho file: {}", doc.getFileUrl());
            }

            return HomestayDocumentResponse.builder()
                    .id(doc.getId())
                    .documentType(doc.getDocumentType())
                    .fileUrl(doc.getFileUrl())
                    .viewUrl(viewUrl)
                    .status(doc.getStatus())
                    .rejectionReason(doc.getRejectionReason())
                     .uploadedAt(doc.getCreatedAt()) // Mở comment nếu Entity có trường này
                    .build();
        }).toList();
    }

    @Override
    public void submitHomestayForReview(Long homestayId, Long hostId) {

    }

    @Override
    public List<PreUploadResponse> prepareHomestayUploads(Long homestayId, Long hostId, HomestayBatchUploadRequest request) {
        if (!homestayRepository.existsByIdAndOwnerId(homestayId, hostId)) {
            throw new AppException(ResultCode.HOMESTAY_NOT_FOUND);
        }
        List<HomestayDocument> documents = request.getItems().stream()
                .map(item -> {

                    String objectKey = mediaUtil.generateObjectKey(homestayId, item);

                    HomestayDocument doc = new HomestayDocument();
                    doc.setHomestayId(homestayId);
                    doc.setDocumentType(item.getDocumentType());
                    doc.setFileUrl(objectKey);
                    doc.setStatus(DocumentStatus.PENDING);

                    return doc;
                })
                .toList();

        List<HomestayDocument> savedDocs = documentRepository.saveAll(documents);

        return IntStream.range(0, savedDocs.size())
                .mapToObj(i -> {
                    HomestayDocument doc = savedDocs.get(i);
                    HomestayDocumentMeta meta = request.getItems().get(i);

                    String uploadUrl = s3Service.generatePresignedPutUrl(
                            doc.getFileUrl(),
                            meta.getContentType(),
                            meta.getFileSize()
                    );

                    return new PreUploadResponse(
                            doc.getId(),
                            meta.getFileName(),
                            doc.getFileUrl(),
                            uploadUrl
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void confirmDocumentUpload(Long homestayId, Long documentId, Long hostId) {
        HomestayDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu này trong hệ thống."));

        document.setStatus(DocumentStatus.PENDING);

        try {
            if (document.getDocumentType() == PropertyDocumentType.OWNERSHIP_CERTIFICATE
                    || document.getDocumentType() == PropertyDocumentType.LEASE_AGREEMENT) {

                byte[] imageBytes = s3Service.downloadFileAsBytes(document.getFileUrl());

                if (imageBytes != null && imageBytes.length > 0) {
                    String extractedText = tesseractService.extractTextFromImage(imageBytes);

                    HostKycProfile hostProfile = hostKycRepository.findByUserId(hostId)
                            .orElseThrow(() -> new RuntimeException("Host chưa KYC"));
                    String legalName = hostProfile.getLegalName().toUpperCase();

                    if (extractedText.contains(legalName)) {
                        document.setStatus(DocumentStatus.APPROVED);
                        log.info("Tesseract Auto-Approve thành công cho tài liệu ID: {}", documentId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi Auto-Verify bằng Tesseract: {}", e.getMessage());
        }

        documentRepository.save(document);
    }


}
