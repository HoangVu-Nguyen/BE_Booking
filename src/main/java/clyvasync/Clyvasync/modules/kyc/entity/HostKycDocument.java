package clyvasync.Clyvasync.modules.kyc.entity;

import clyvasync.Clyvasync.enums.kyc.KycDocumentStatus;
import clyvasync.Clyvasync.enums.kyc.KycDocumentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "host_kyc_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostKycDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profile_id", nullable = false)
    private Long profileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", length = 50, nullable = false)
    private KycDocumentType documentType;

    @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
    private String fileUrl;
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private KycDocumentStatus status;

    @Column(name = "rejection_note", columnDefinition = "TEXT")
    private String rejectionNote;
    @Column(name = "ai_score", precision = 5, scale = 2)
    private BigDecimal aiScore;
    @Column(name = "ocr_data", columnDefinition = "TEXT")
    private String ocrData;


    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private OffsetDateTime uploadedAt;
}