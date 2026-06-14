package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RatePlanCalendar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface RatePlanCalendarRepository extends JpaRepository<RatePlanCalendar, Long> {
    List<RatePlanCalendar> findByRatePlanIdInAndNightDateBetween(List<Long> ratePlanIds, LocalDate start, LocalDate end);


    Optional<RatePlanCalendar> findByRatePlanIdAndNightDate(
            Long ratePlanId,
            LocalDate nightDate
    );
}
