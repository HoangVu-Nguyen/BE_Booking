package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.modules.tour.entity.TourAvailability;
import clyvasync.Clyvasync.repository.tour.TourAvailabilityRepository;
import clyvasync.Clyvasync.service.tour.TourAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourAvailabilityServiceImpl implements TourAvailabilityService {
    private final TourAvailabilityRepository tourAvailabilityRepository;
    @Override
    public int deductTourSlots(Long availabilityId, int slots) {
        return tourAvailabilityRepository.deductTourSlots(availabilityId, slots);
    }

    @Override
    public List<TourAvailability> findByIdIn(List<Long> ids) {
        return tourAvailabilityRepository.findByIdIn(ids);
    }
}
