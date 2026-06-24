package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.AmenityAlias;
import org.springframework.data.jpa.repository.JpaRepository;

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
}