package clyvasync.Clyvasync.repository.kyc;

import clyvasync.Clyvasync.enums.kyc.KycDocumentType;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostKycDocumentRepository extends JpaRepository<HostKycDocument, Long> {
    List<HostKycDocument> findByProfileId(Long profileId);
    Optional<HostKycDocument> findByProfileIdAndDocumentType(Long profileId, KycDocumentType type);

}
