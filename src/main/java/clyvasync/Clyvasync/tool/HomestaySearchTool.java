package clyvasync.Clyvasync.tool;

import clyvasync.Clyvasync.dto.record.AiSearchRequest;
import clyvasync.Clyvasync.dto.record.AmenityMappingResult;
import clyvasync.Clyvasync.dto.record.PolicyFilterRequest;
import clyvasync.Clyvasync.dto.request.GlobalSearchRequest;
import clyvasync.Clyvasync.dto.response.GlobalSearchResponse;
import clyvasync.Clyvasync.service.homestay.AmenityMappingService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.tool.criteria.HomeSearchCriteria;
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
    private final AmenityMappingService amenityMappingService;


    @Tool(description = "BẮT BUỘC SỬ DỤNG công cụ này khi người dùng có nhu cầu tìm kiếm homestay, phòng nghỉ, khách sạn.")
    public List<GlobalSearchResponse> searchHomestay(HomeSearchCriteria homeSearchCriteria
                                                     ) {
        System.out.println("\n====== 🤖 GEMINI ĐÃ BÓC TÁCH THÔNG TIN ======");
        System.out.println("📍 Địa điểm       : " + homeSearchCriteria.location());
        System.out.println("📅 Check-in/out   : " + homeSearchCriteria.checkInDate() + " -> " + homeSearchCriteria.checkOutDate());
        System.out.println("👥 Số khách/giường: " + homeSearchCriteria.guests() + " khách, " + homeSearchCriteria.bedCount() + " giường");
        System.out.println("💰 Mức giá        : " + homeSearchCriteria.minPrice() + " -> " + homeSearchCriteria.maxPrice());
        System.out.println("🧠 Ngữ nghĩa      : " + homeSearchCriteria.semanticQuery());
        System.out.println("=============================================");
        System.out.println("AI bóc ra tiện ích: " + homeSearchCriteria.requestedAmenities());

        AmenityMappingResult mappingResult = amenityMappingService.processAiAmenities(homeSearchCriteria.requestedAmenities());
        System.out.println(mappingResult.mappedIds());
        String finalSemanticQuery = (homeSearchCriteria.semanticQuery() != null ? homeSearchCriteria.semanticQuery() : "")
                + " " + mappingResult.unmappedKeywords();
       // PolicyFilterRequest policyFilter = extractPolicies(policyCodes);
        // Lưu ý: Ông cần vào class GlobalSearchRequest để bổ sung thêm các trường này
        // để nó khớp với constructor dưới đây.
        AiSearchRequest request = new AiSearchRequest(
                homeSearchCriteria.location(),
                homeSearchCriteria.homestayName(),
                homeSearchCriteria.guests(),
                homeSearchCriteria.bedCount(),
                homeSearchCriteria.minPrice(),
                homeSearchCriteria.maxPrice(),
                homeSearchCriteria.checkInDate(),
                homeSearchCriteria.checkOutDate(),
                mappingResult.mappedIds(), // Danh sách ID tiện ích (Tầng 1)
                null,              // Bộ lọc chính sách (Tầng 1)
                finalSemanticQuery.trim()  // Chuỗi ngữ nghĩa dồn lại cho pgvector (Tầng 2)
        );

        System.out.println("⏳ ĐANG GỌI SERVICE DB VỚI DỮ LIỆU: " + request);

        List<GlobalSearchResponse> results = homestayService.aiHybridSearch(request);

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
    private PolicyFilterRequest extractPolicies(List<String> policyCodes) {
        if (policyCodes == null || policyCodes.isEmpty()) {
            return new PolicyFilterRequest(null, null, null, null, null);
        }

        return new PolicyFilterRequest(
                policyCodes.contains("PETS") ? true : null,
                policyCodes.contains("SMOKING") ? true : null,
                policyCodes.contains("PARTIES") ? true : null,
                policyCodes.contains("CHILDREN") ? true : null,
                policyCodes.contains("NO_DEPOSIT") ? true : null
        );
    }
}