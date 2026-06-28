package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomestayDocumentRepository extends JpaRepository<HomestayDocument, Long> {

    List<HomestayDocument> findByHomestayId(Long homestayId);

    @Query("SELECT COUNT(d) FROM HomestayDocument d WHERE d.homestayId = :homestayId AND d.status = 'APPROVED'")
    long countApprovedDocuments(@Param("homestayId") Long homestayId);
    List<HomestayDocument> findByHomestayIdOrderByCreatedAtDesc(Long homestayId);

    long countByHomestayId(Long homestayId);
    boolean existsByHomestayId(Long homestayId);
    List<HomestayDocument> findByHomestayIdIn(List<Long> homestayIds);
}