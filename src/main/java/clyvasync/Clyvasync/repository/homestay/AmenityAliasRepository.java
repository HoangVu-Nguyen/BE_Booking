package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.AmenityAlias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AmenityAliasRepository extends JpaRepository<AmenityAlias, Long> {

    List<AmenityAlias> findByAmenityId(Integer amenityId);

    Optional<AmenityAlias> findByAmenityIdAndNormalizedAlias(
            Integer amenityId,
            String normalizedAlias
    );

    boolean existsByAmenityIdAndNormalizedAlias(
            Integer amenityId,
            String normalizedAlias
    );
    @Query(value = "SELECT amenity_id FROM amenity_alias " +
            "WHERE similarity(normalized_alias, :keyword) > 0.4 " +
            "ORDER BY similarity(normalized_alias, :keyword) DESC LIMIT 1",
            nativeQuery = true)
    Integer findBestMatchAmenityId(@Param("keyword") String keyword);
}