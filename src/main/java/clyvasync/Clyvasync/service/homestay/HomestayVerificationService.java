package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.record.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.request.DocumentUploadRequest;
import clyvasync.Clyvasync.dto.request.HomestayBatchUploadRequest;
import clyvasync.Clyvasync.dto.response.HomestayDocumentResponse;
import clyvasync.Clyvasync.dto.response.PreUploadResponse;

import java.util.List;

public interface HomestayVerificationService {
    HomestayDocumentResponse addDocument(Long homestayId, Long hostId, DocumentUploadRequest request);
    List<HomestayDocumentResponse> getDocuments(Long homestayId, Long hostId);
    void submitHomestayForReview(Long homestayId, Long hostId);
    List<PreUploadResponse> prepareHomestayUploads(Long homestayId, Long hostId, HomestayBatchUploadRequest request);
    void confirmDocumentUpload(Long homestayId, Long documentId, Long hostId);
    void submitHomestayForVerification(Long homestayId, Long hostId);
}
