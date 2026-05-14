package clyvasync.Clyvasync.dto.request;

import lombok.Data;

@Data
public class UpdateBookingDraftRequest {
    private String guestName;
    private String email;
    private String phone;
    private String specialRequests; // Lời nhắn cho Butler/Chủ nhà
}