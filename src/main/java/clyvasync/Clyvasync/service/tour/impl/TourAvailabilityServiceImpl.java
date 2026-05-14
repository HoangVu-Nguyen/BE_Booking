package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.repository.tour.TourAvailabilityRepository;
import clyvasync.Clyvasync.service.tour.TourAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TourAvailabilityServiceImpl implements TourAvailabilityService {
    private final TourAvailabilityRepository tourAvailabilityRepository;
    @Override
    public int deductTourSlots(Long availabilityId, int slots) {
        return tourAvailabilityRepository.deductTourSlots(availabilityId, slots);
    }
}
