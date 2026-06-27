package clyvasync.Clyvasync.repository.kyc;

import clyvasync.Clyvasync.modules.kyc.entity.HostKycDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostKycDocumentRepository extends JpaRepository<HostKycDocument, Long> {
    List<HostKycDocument> findByProfileId(Long profileId);

}
