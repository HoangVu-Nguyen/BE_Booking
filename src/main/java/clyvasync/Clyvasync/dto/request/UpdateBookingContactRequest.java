package clyvasync.Clyvasync.dto.request;

import lombok.Data;

@Data
public class UpdateBookingContactRequest {
    private String guestName;
    private String phone;
    private String email;
    private String specialRequests;
}