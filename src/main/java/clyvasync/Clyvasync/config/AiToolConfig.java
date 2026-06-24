package clyvasync.Clyvasync.config;

import clyvasync.Clyvasync.dto.record.HomestaySearchRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class AiToolConfig {

    @Bean
    @Description("BẮT BUỘC SỬ DỤNG công cụ này khi người dùng có nhu cầu tìm kiếm homestay, phòng nghỉ.")
    public Function<HomestaySearchRequest, String> searchHomestayTool() {
        return request -> {
            // CHỈ IN RA CONSOLE ĐỂ KIỂM TRA XEM AI CÓ BẮT ĐÚNG TỪ KHÓA KHÔNG
            System.out.println("====== GEMINI ĐÃ TỰ ĐỘNG GỌI HÀM NÀY ======");
            System.out.println("Thành phố: " + request.city());
            System.out.println("Giá Max: " + request.maxPrice());
            System.out.println("Ngữ nghĩa: " + request.semanticQuery());
            System.out.println("=========================================");

            // Trả về một câu giả lập để Gemini đọc
            return "Hệ thống giả lập tìm thấy: Phòng VIP giá 1 triệu tại " + request.city();
        };
    }
    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}