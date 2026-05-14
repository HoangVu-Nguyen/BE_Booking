package clyvasync.Clyvasync.service.tour;

import org.springframework.data.repository.query.Param;

public interface TourAvailabilityService {
    int deductTourSlots(Long availabilityId,  int slots);
}
