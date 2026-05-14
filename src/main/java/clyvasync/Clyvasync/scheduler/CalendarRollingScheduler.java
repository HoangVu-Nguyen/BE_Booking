package clyvasync.Clyvasync.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CalendarRollingScheduler {

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void rollCalendarForward() {
        String sql = """
            INSERT INTO room_calendar (room_id, night_date, available_quantity)
            SELECT id, (CURRENT_DATE + INTERVAL '365 days')::date, quantity
            FROM homestay_rooms
            WHERE status = 'ACTIVE'
            ON CONFLICT (room_id, night_date) DO NOTHING;
            """;

        int rowsInserted = jdbcTemplate.update(sql);
        System.out.println("[Clyvasync Cron] Đã tự động gia hạn phòng trống cho 365 ngày tới. Số dòng ảnh hưởng: " + rowsInserted);
    }
}