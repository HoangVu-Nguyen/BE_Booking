package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.type.ChatType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConversationSummaryResponse {
    private Long id;

    private ChatType type; // ADMIN, HOST, GROUP

    // Tùy theo ngữ cảnh: Nếu là 1-1 thì là tên Khách/Chủ. Nếu là Group thì là tên Nhóm (VD: Tour Đà Lạt 3N2Đ)
    private String targetName;

    private String targetAvatar;

    private String lastMessage;

    private String lastMessageTime; // Format thành chuỗi thân thiện (VD: "10:30", "Hôm qua") ở phía Backend

    private long unreadCount;

    // Trạng thái đơn hàng để render badge màu xanh/đỏ dưới tên
    private String bookingStatus;

    // Tên property để hiển thị dòng chữ xám nhỏ dưới tên (VD: "Clyvasync Villa - Đà Lạt")
    private String propertyName;
}