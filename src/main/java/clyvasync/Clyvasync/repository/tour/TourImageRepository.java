package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage,Long> {
    @Query("SELECT ti FROM TourImage ti WHERE ti.tourId IN :tourIds " +
            "ORDER BY ti.isPrimary DESC, ti.id ASC")
    List<TourImage> findImagesForHover(@Param("tourIds") List<Long> tourIds);
}
