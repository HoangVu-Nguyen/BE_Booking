package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.modules.tour.entity.TourAvailability;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.repository.tour.TourAvailabilityRepository;
import clyvasync.Clyvasync.service.tour.TourAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public int releaseTourSlots(Long availabilityId, int slots) {
        return tourAvailabilityRepository.releaseTourSlots(availabilityId, slots);
    }

    @Override
    public void releaseTourSlotsBatch(List<TourBooking> tourBookings) {
        // 1. Gom tất cả ID cần update
        List<Long> availabilityIds = tourBookings.stream()
                .map(TourBooking::getAvailabilityId)
                .distinct()
                .toList();

        // 2. Query 1 lần lấy toàn bộ Availabilities (SELECT ... WHERE id IN (...))
        List<TourAvailability> availabilities = tourAvailabilityRepository.findAllById(availabilityIds);

        // 3. Gom nhóm số lượng slot cần trả lại theo từng AvailabilityId
        // (Phòng trường hợp 1 availability có nhiều booking khác nhau trong cùng 1 list)
        Map<Long, Integer> slotsToRestoreMap = tourBookings.stream()
                .collect(Collectors.groupingBy(
                        TourBooking::getAvailabilityId,
                        Collectors.summingInt(TourBooking::getParticipantCount)
                ));

        // 4. Cập nhật số lượng trên RAM
        for (TourAvailability availability : availabilities) {
            int slotsToRestore = slotsToRestoreMap.getOrDefault(availability.getId(), 0);
            availability.setRemainingSlots(availability.getRemainingSlots() + slotsToRestore);
        }

        // 5. Lưu xuống DB 1 lần.
        // (Nếu spring.jpa.properties.hibernate.jdbc.batch_size được bật, nó sẽ gộp thành 1 lệnh DB)
        tourAvailabilityRepository.saveAll(availabilities);
    }
}
