package clyvasync.Clyvasync.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record RoomTimelineResponse(
        Long roomId,
        String roomName,
        List<DailyStatusResponse> dailyStatuses, // Dành cho việc hiển thị giá từng ngày
        List<BookingBlockResponse> bookings      // Dành cho các cục đen đen (khách đặt)
) {}
