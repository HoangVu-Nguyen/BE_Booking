package clyvasync.Clyvasync.service.tour;

import clyvasync.Clyvasync.modules.tour.entity.TourAvailability;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TourAvailabilityService {
    int deductTourSlots(Long availabilityId,  int slots);
    List<TourAvailability> findByIdIn(List<Long> ids);
}
