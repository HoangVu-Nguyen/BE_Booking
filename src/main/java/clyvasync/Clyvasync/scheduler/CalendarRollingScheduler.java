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

    @Scheduled(cron = "0 0 0 * * *") // Chạy 0 giờ sáng mỗi ngày
    @Transactional
    public void rollCalendarForward() {
        String sql = """
        INSERT INTO room_calendar (room_id, night_date, available_quantity)
        SELECT r.id, d.day::date, r.quantity
        FROM homestay_rooms r
        CROSS JOIN generate_series(
            CURRENT_DATE + INTERVAL '1 day', 
            CURRENT_DATE + INTERVAL '365 days', 
            '1 day'::interval
        ) AS d(day)
        WHERE r.status = 'ACTIVE'
        ON CONFLICT (room_id, night_date) DO NOTHING;
        """;

        int rowsInserted = jdbcTemplate.update(sql);
        System.out.println("[Clyvasync Cron] Đã đồng bộ lịch 365 ngày tới. Số bản ghi mới được tạo: " + rowsInserted);
    }
}