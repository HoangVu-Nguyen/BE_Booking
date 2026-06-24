package service.ai.impl;

import com.pgvector.PGvector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.embedding.EmbeddingModel; // Import chuẩn của Spring AI
import service.ai.SearchSyncService;

@Service
public class SearchSyncServiceImpl implements SearchSyncService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;

    public SearchSyncServiceImpl(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
    }

    @Override // Đừng quên override từ interface
    @Transactional
    public void syncRoomToIndex(Long roomId, Long homestayId, String homestayName, String city, int beds, int guests, double price, String allAmenitiesText) {

        // 0. Xử lý an toàn chuỗi tiện ích (Tránh lỗi Null)
        String safeAmenities = (allAmenitiesText != null) ? allAmenitiesText : "";

        // 1. Gọi AI để nhúng (embed) toàn bộ nội dung thành Vector
        // Lời gọi này sẽ tự động chạy qua API của Gemini nhờ cấu hình application.properties
        String textToEmbed = "Homestay " + homestayName + " tại " + city + ". Tiện ích: " + safeAmenities;
        float[] embeddingArray = embeddingModel.embed(textToEmbed);
        PGvector pgVector = new PGvector(embeddingArray);

        // 2. Định dạng lại chuỗi tiện ích cho PostgreSQL Full-text Search (tsquery)
        // Ví dụ: "wifi, hồ bơi" -> "wifi & hồ_bơi"
        String tsQueryFormat = "";
        if (!safeAmenities.trim().isEmpty()) {
            tsQueryFormat = safeAmenities.replace(", ", " & ").replace(" ", "_");
        }

        // 3. Câu lệnh SQL
        String sql = """
            INSERT INTO homestay_search_index 
            (room_id, homestay_id, name, city, bed_count, max_guests, price_current, amenities_tsv, embedding)
            VALUES (?, ?, ?, ?, ?, ?, ?, to_tsquery(?), ?)
            ON CONFLICT (room_id) DO UPDATE SET
            price_current = EXCLUDED.price_current,
            amenities_tsv = EXCLUDED.amenities_tsv,
            embedding = EXCLUDED.embedding
        """;

        // 4. Lưu xuống database (Đã sửa 1L thành homestayId để dữ liệu động)
        // Nếu tsQueryFormat rỗng, postgres có thể báo lỗi to_tsquery, ta truyền một từ khóa vô thưởng vô phạt hoặc để null tùy logic.
        // Ở đây tạm truyền chuỗi đã format.
        jdbcTemplate.update(sql, roomId, homestayId, homestayName, city, beds, guests, price,
                tsQueryFormat.isEmpty() ? null : tsQueryFormat,
                pgVector);
    }
}