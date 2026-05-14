package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingInitResponse {
    private String bookingCode;
    private Long bookingId;
}