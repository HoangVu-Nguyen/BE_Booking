package clyvasync.Clyvasync.dto.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class BookingEvent extends ApplicationEvent {
    private final Long hostId;          // ID của chủ nhà để bắn thông báo riêng
    private final Object bookingPayload; // Chứa thông tin đơn đặt phòng (mã đơn, tên phòng, ngày đặt...)

    public BookingEvent(Object source, Long hostId, Object bookingPayload) {
        super(source);
        this.hostId = hostId;
        this.bookingPayload = bookingPayload;
    }
}