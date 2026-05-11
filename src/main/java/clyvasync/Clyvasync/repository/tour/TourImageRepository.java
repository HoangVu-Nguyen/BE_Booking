package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage,Long> {
}
