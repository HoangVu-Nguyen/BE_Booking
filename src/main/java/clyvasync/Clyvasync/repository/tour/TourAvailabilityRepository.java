package clyvasync.Clyvasync.repository.tour;

import clyvasync.Clyvasync.modules.tour.entity.TourAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourAvailabilityRepository extends JpaRepository<TourAvailability, Long> {
    @Modifying
    @Query("""
    UPDATE TourAvailability ta 
    SET ta.remainingSlots = ta.remainingSlots - :slots
    WHERE ta.id = :availabilityId AND ta.remainingSlots >= :slots AND ta.isActive = true
    """)
    int deductTourSlots(@Param("availabilityId") Long availabilityId, @Param("slots") int slots);
    @Modifying
    @Query("""
    UPDATE TourAvailability ta 
    SET ta.remainingSlots = ta.remainingSlots + :slots
    WHERE ta.id = :availabilityId
    """)
    int releaseTourSlots(@Param("availabilityId") Long availabilityId,
                         @Param("slots") int slots);
    List<TourAvailability> findByIdIn(List<Long> ids);
}
