package clyvasync.Clyvasync.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class RoomBookingTool {

    @Tool(description = "Sử dụng khi khách muốn KIỂM TRA PHÒNG CÒN TRỐNG hay không trong một khoảng thời gian.")
    public String checkAvailability(
            @ToolParam(description = "ID của phòng cần kiểm tra") Long roomId,
            @ToolParam(description = "Ngày check-in, định dạng YYYY-MM-DD") String checkInDate,
            @ToolParam(description = "Ngày check-out, định dạng YYYY-MM-DD") String checkOutDate
    ) {
        System.out.println("--- GỌI TOOL CHECK PHÒNG TRỐNG ---");
        // Gọi DB kiểm tra xem phòng này ngày đó có ai đặt chưa...
        return "Phòng hiện tại vẫn còn trống trong các ngày này, có thể cho khách đặt.";
    }
}