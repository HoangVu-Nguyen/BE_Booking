package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.type.ChatType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConversationDetailResponse {
    private Long id;
    private ChatType type;

    // Thông tin người đang chat (Cột 2 - Header)
    private String targetName;
    private String targetAvatar;

    // Thông tin Đơn hàng/Tour (Cột 3 - CRM Panel)
    private BookingDetailInfo bookingDetails;

    // Lịch sử tin nhắn (Tải sẵn 20 tin gần nhất)
    private List<MessageResponse> messages;

    @Data
    @Builder
    public static class BookingDetailInfo {
        private String code;
        private String propertyName;
        private String propertyImage;
        private String checkIn;
        private String checkOut;
        private int guests;
        private double totalPrice;
        private String paymentStatus; // PAID, PENDING, REFUNDED
        private String bookingStatus; // CONFIRMED, COMPLETED, CANCELLED
    }
}