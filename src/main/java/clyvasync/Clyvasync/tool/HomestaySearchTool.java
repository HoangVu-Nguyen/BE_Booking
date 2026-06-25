package clyvasync.Clyvasync.tool;

import clyvasync.Clyvasync.dto.request.GlobalSearchRequest;
import clyvasync.Clyvasync.dto.response.GlobalSearchResponse;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import lombok.AllArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class HomestaySearchTool {
    private final HomestayService homestayService;


    @Tool(description = "BẮT BUỘC SỬ DỤNG công cụ này khi người dùng có nhu cầu tìm kiếm homestay, phòng nghỉ, khách sạn.")
    public List<GlobalSearchResponse> searchHomestay(
            // --- 1. NHÓM ĐỊA ĐIỂM ---
            @ToolParam(description = "Tên thành phố hoặc địa danh (Ví dụ: Đà Lạt, Vũng Tàu, Quận 1...)")
            String location,

            // --- 2. NHÓM THỜI GIAN & SỨC CHỨA ---
            @ToolParam(description = "Ngày nhận phòng (Check-in), định dạng YYYY-MM-DD", required = false)
            String checkInDate,

            @ToolParam(description = "Ngày trả phòng (Check-out), định dạng YYYY-MM-DD", required = false)
            String checkOutDate,

            @ToolParam(description = "Tổng số lượng khách (người lớn + trẻ em)", required = false)
            Integer guests,

            @ToolParam(description = "Số lượng phòng ngủ hoặc giường", required = false)
            Integer bedrooms,

            // --- 3. NHÓM TÀI CHÍNH ---
            @ToolParam(description = "Giá TỐI THIỂU người dùng muốn tìm (VNĐ)", required = false)
            Double minPrice,

            @ToolParam(description = "Giá TỐI ĐA người dùng muốn tìm (VNĐ)", required = false)
            Double maxPrice,

            // --- 4. NHÓM CHÍNH SÁCH (POLICIES) ---
            @ToolParam(description = "Khách có mang theo thú cưng không? Trả về true nếu có", required = false)
            Boolean allowPets,

            @ToolParam(description = "Khách có yêu cầu phòng được phép hút thuốc không? Trả về true nếu có", required = false)
            Boolean allowSmoking,
            @ToolParam(description = "Mảng chữ chứa tiện ích. VD: ['wifi', 'bể bơi']")
            List<String> requestedAmenities,

            // --- 5. NHÓM NGỮ NGHĨA & TIỆN ÍCH (AI SẼ GOM HẾT VÀO ĐÂY) ---
            @ToolParam(description = "Gom TẤT CẢ các yêu cầu về tiện ích (hồ bơi, wifi, máy chiếu, bồn tắm...) và cảm giác (view đẹp, yên tĩnh, lãng mạn...) thành một câu tìm kiếm tự nhiên. KHÔNG đưa giá, địa điểm, ngày tháng vào đây.")
            String semanticQuery
    ) {
        System.out.println("\n====== 🤖 GEMINI ĐÃ BÓC TÁCH THÔNG TIN ======");
        System.out.println("📍 Địa điểm       : " + location);
        System.out.println("📅 Check-in/out   : " + checkInDate + " -> " + checkOutDate);
        System.out.println("👥 Số khách/giường: " + guests + " khách, " + bedrooms + " giường");
        System.out.println("💰 Mức giá        : " + minPrice + " -> " + maxPrice);
        System.out.println("🐶 Thú cưng       : " + allowPets);
        System.out.println("🚬 Hút thuốc      : " + allowSmoking);
        System.out.println("🧠 Ngữ nghĩa      : " + semanticQuery);
        System.out.println("=============================================");
        System.out.println("AI bóc ra tiện ích: " + requestedAmenities);

        List<Integer> actualAmenityIds = new ArrayList<>();

        // Lưu ý: Ông cần vào class GlobalSearchRequest để bổ sung thêm các trường này
        // để nó khớp với constructor dưới đây.
        GlobalSearchRequest request = new GlobalSearchRequest(
                location,
                null, // category
                minPrice != null ? BigDecimal.valueOf(minPrice) : null,
                maxPrice != null ? BigDecimal.valueOf(maxPrice) : null,
                guests,
                bedrooms,
                null, // minRating
                null, // amenityIds (Có thể null vì ta dùng semanticQuery để quét rồi)
                checkInDate,  // Thêm trường này vào Request
                checkOutDate, // Thêm trường này vào Request
                allowPets,    // Thêm trường này vào Request
                allowSmoking  // Thêm trường này vào Request
        );

        System.out.println("⏳ ĐANG GỌI SERVICE DB VỚI DỮ LIỆU: " + request);

        List<GlobalSearchResponse> results = homestayService.cinematicSearch(request);

        if (results == null || results.isEmpty()) {
            System.out.println("❌ Không tìm thấy phòng nào khớp yêu cầu!");
        } else {
            System.out.println("✅ Tìm thấy " + results.size() + " phòng.");
            System.out.println("👉 Top 1: " + results.get(0).name() + " - " + results.get(0).basePrice());
        }
        System.out.println("---------------------------------------------\n");

        if (results != null && results.size() > 5) {
            return results.subList(0, 5);
        }
        return results;
    }
}