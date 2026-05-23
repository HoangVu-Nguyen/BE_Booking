package clyvasync.Clyvasync.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingSimpleInfo {
    private String bookingCode;
    private String guestName;
    private int quantity;
}