package clyvasync.Clyvasync.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class HomestaySearchTool {

    @Tool(description = "BẮT BUỘC SỬ DỤNG công cụ này khi người dùng có nhu cầu tìm kiếm homestay, phòng nghỉ.")
    public String searchHomestay(
            @ToolParam(description = "Tên thành phố người dùng muốn tìm, ví dụ: Đà Lạt, Nha Trang, TP HCM")
            String city,

            @ToolParam(description = "Giá tối đa người dùng muốn tìm. Nếu không nói giá thì truyền null", required = false)
            Double maxPrice,

            @ToolParam(description = "Câu mô tả nhu cầu tìm kiếm tự nhiên của người dùng")
            String semanticQuery
    ) {
        System.out.println("====== GEMINI ĐÃ TỰ ĐỘNG GỌI HÀM NÀY ======");
        System.out.println("Thành phố: " + city);
        System.out.println("Giá Max: " + maxPrice);
        System.out.println("Ngữ nghĩa: " + semanticQuery);
        System.out.println("=========================================");

        return "Hệ thống giả lập tìm thấy: Phòng VIP giá 1 triệu tại " + city;
    }
}