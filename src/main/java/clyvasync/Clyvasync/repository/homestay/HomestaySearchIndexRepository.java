package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestaySearchIndex;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // <-- THÊM IMPORT NÀY
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// THÊM JpaSpecificationExecutor VÀO ĐÂY
public interface HomestaySearchIndexRepository extends
        JpaRepository<HomestaySearchIndex, Long>,
        JpaSpecificationExecutor<HomestaySearchIndex> {

    /**
     * TÌM KIẾM VECTOR TẦNG 2
     */
    @Query(value = "SELECT * FROM homestay_search_index " +
            "WHERE room_id IN (:candidateRoomIds) " +
            "ORDER BY embedding <=> CAST(:vector AS vector) " +
            "LIMIT :limit",
            nativeQuery = true)
    List<HomestaySearchIndex> findSemanticWithinCandidates(
            @Param("candidateRoomIds") List<Long> candidateRoomIds,
            @Param("vector") String vectorString,
            @Param("limit") int limit
    );

}
