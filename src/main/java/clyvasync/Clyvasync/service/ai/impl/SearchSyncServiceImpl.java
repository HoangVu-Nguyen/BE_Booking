package clyvasync.Clyvasync.service.ai.impl;

import clyvasync.Clyvasync.service.ai.SearchSyncService;
import com.pgvector.PGvector;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.embedding.EmbeddingModel; // Import chuẩn của Spring AI

import java.util.Map;

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
        // 3. Câu lệnh SQL đã được tối ưu
        String sql = """
        INSERT INTO homestay_search_index 
        (room_id, homestay_id, name, city, bed_count, max_guests, price_current, amenities_tsv, embedding)
        VALUES (?, ?, ?, ?, ?, ?, ?, to_tsvector('simple', COALESCE(?, '')), ?)
        ON CONFLICT (room_id) DO UPDATE SET
            homestay_id = EXCLUDED.homestay_id,
            name = EXCLUDED.name,
            city = EXCLUDED.city,
            bed_count = EXCLUDED.bed_count,
            max_guests = EXCLUDED.max_guests,
            price_current = EXCLUDED.price_current,
            amenities_tsv = to_tsvector('simple', COALESCE(EXCLUDED.amenities_tsv::text, '')),
            embedding = EXCLUDED.embedding
    """;

        // 4. Lưu xuống database (Đã sửa 1L thành homestayId để dữ liệu động)
        // Nếu tsQueryFormat rỗng, postgres có thể báo lỗi to_tsquery, ta truyền một từ khóa vô thưởng vô phạt hoặc để null tùy logic.
        // Ở đây tạm truyền chuỗi đã format.
        jdbcTemplate.update(sql, roomId, homestayId, homestayName, city, beds, guests, price,
                tsQueryFormat.isEmpty() ? null : tsQueryFormat,
                pgVector);
    }

    @Transactional
    public void triggerSyncForRoom(Long roomId) {
        String fetchSql = """
    SELECT 
        r.id AS room_id, 
        r.homestay_id, 
        h.name AS homestay_name,
        
        -- ĐÃ SỬA LỖI: Thêm định danh l.name rõ ràng
        COALESCE((SELECT l.city_name FROM locations l WHERE l.id = h.location_id), h.address_detail) AS city,
        
        r.name AS room_name, 
        r.bed_count, 
        r.max_guests,
        
        -- Lấy giá rẻ nhất từ các gói giá của phòng này làm giá hiển thị
        COALESCE((SELECT MIN(price) FROM room_rate_plans WHERE room_id = r.id), 0) AS min_price,
        
        -- Gom các loại giường (VD: 1 DOUBLE, 2 SINGLE)
        (
            SELECT string_agg(quantity || ' ' || bed_type, ', ') 
            FROM room_beds 
            WHERE room_id = r.id
        ) AS beds_text,
        
        -- Gom các tiện ích nổi bật của phòng (kèm theo display_value nếu có)
        (
            SELECT string_agg(a.name || COALESCE(' (' || rah.display_value || ')', ''), ', ') 
            FROM room_amenity_highlights rah 
            JOIN amenities a ON rah.amenity_id = a.id 
            WHERE rah.room_id = r.id
        ) AS room_amenities,
        
        -- Gom các quyền lợi đi kèm từ TẤT CẢ các gói giá của phòng
        (
            SELECT string_agg(DISTINCT a.name, ', ')
            FROM rate_plan_benefit_mapping rpbm
            JOIN room_rate_plans rrp ON rpbm.rate_plan_id = rrp.id
            JOIN amenities a ON rpbm.amenity_id = a.id
            WHERE rrp.room_id = r.id
        ) AS plan_benefits

    FROM homestay_rooms r
    JOIN homestays h ON r.homestay_id = h.id
    WHERE r.id = ?
""";

        try {
            // 2. Lấy dữ liệu gộp
            Map<String, Object> data = jdbcTemplate.queryForMap(fetchSql, roomId);

            Long homestayId = ((Number) data.get("homestay_id")).longValue();
            String homestayName = (String) data.get("homestay_name");
            String roomName = (String) data.get("room_name");
            String city = (String) data.get("city");
            int bedCount = ((Number) data.get("bed_count")).intValue();
            int maxGuests = ((Number) data.get("max_guests")).intValue();
            double price = ((Number) data.get("min_price")).doubleValue();

            // 3. Nối chuỗi để mớm cho AI
            String bedsText = (String) data.get("beds_text");
            String roomAmenities = (String) data.get("room_amenities");
            String planBenefits = (String) data.get("plan_benefits");

            StringBuilder contextBuilder = new StringBuilder();
            if (bedsText != null) contextBuilder.append("Cấu trúc giường: ").append(bedsText).append(". ");
            if (roomAmenities != null) contextBuilder.append("Tiện ích phòng: ").append(roomAmenities).append(". ");
            if (planBenefits != null) contextBuilder.append("Quyền lợi gói giá: ").append(planBenefits).append(". ");

            String finalContext = contextBuilder.toString();

            // 4. Gọi lại hàm sync gốc để nhúng Vector và lưu DB
            syncRoomToIndex(roomId, homestayId, homestayName + " - " + roomName, city, bedCount, maxGuests, price, finalContext);

        } catch (EmptyResultDataAccessException e) {
            // Nếu không tìm thấy phòng (có thể đã bị xóa cứng), ta có thể log hoặc xóa khỏi search index
            jdbcTemplate.update("DELETE FROM homestay_search_index WHERE room_id = ?", roomId);
        }
    }
}