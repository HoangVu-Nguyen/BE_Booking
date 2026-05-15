package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.dto.projection.TourImageProjection;
import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage,Long> {
    @Query("SELECT ti FROM TourImage ti WHERE ti.tourId IN :tourIds " +
            "ORDER BY ti.isPrimary DESC, ti.id ASC")
    List<TourImage> findImagesForHover(@Param("tourIds") List<Long> tourIds);
    Optional<TourImage> findFirstByTourIdAndIsPrimaryTrue(Long tourId);
    @Query("SELECT ti.tourId AS tourId, ti.imageUrl AS imageUrl FROM TourImage ti " +
            "WHERE ti.tourId IN :tourIds AND ti.isPrimary = true")
    List<TourImageProjection> findPrimaryImagesProjection(@Param("tourIds") List<Long> tourIds);

    default Map<Long, String> getPrimaryImagesByTourIds(List<Long> tourIds) {
        if (tourIds == null || tourIds.isEmpty()) return Map.of();

        return findPrimaryImagesProjection(tourIds).stream()
                .collect(Collectors.toMap(
                        TourImageProjection::getTourId,
                        TourImageProjection::getImageUrl,
                        (existing, replacement) -> existing
                ));
    }    }
