package clyvasync.Clyvasync.modules.kyc.entity;

import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "host_kyc_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostKycProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mapping chay qua ID thay vì @OneToOne với User
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "id_card_number", length = 20, nullable = false, unique = true)
    private String idCardNumber;

    @Column(name = "id_card_issued_date")
    private LocalDate idCardIssuedDate;

    @Column(name = "id_card_issued_by")
    private String idCardIssuedBy;

    @Column(name = "bank_name", length = 100, nullable = false)
    private String bankName;

    @Column(name = "bank_account_number", length = 50, nullable = false)
    private String bankAccountNumber;

    @Column(name = "bank_account_owner", nullable = false)
    private String bankAccountOwner;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, nullable = false)
    private KycProfileStatus status;

    @Column(name = "reviewed_by")
    private Long reviewedBy; // Admin ID

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Version
    @Column(nullable = false)
    private Integer version;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}