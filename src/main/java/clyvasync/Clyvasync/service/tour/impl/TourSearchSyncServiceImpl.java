package clyvasync.Clyvasync.service.tour.impl;

import com.pgvector.PGvector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.embedding.EmbeddingModel;

@Service
public class TourSearchSyncServiceImpl {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;

    public TourSearchSyncServiceImpl(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
    }

    @Transactional
    public void syncTourToIndex(Long tourId) {
        // 1. Kéo toàn bộ dữ liệu Tour, Danh mục và Điểm nhấn (Highlights) bằng 1 câu SQL
        String fetchSql = """
            SELECT 
                t.id AS tour_id, 
                t.homestay_id, 
                tc.name AS category_name, 
                t.name, 
                t.price_per_person, 
                t.duration_type, 
                t.duration_value,
                
           
                (
                    SELECT string_agg(
                        COALESCE(thm.custom_description, thc.default_description, thc.name), 
                        ', '
                    )
                    FROM tour_highlight_mappings thm
                    JOIN tour_highlights_catalog thc ON thm.highlight_id = thc.id
                    WHERE thm.tour_id = t.id AND thm.is_included = true
                ) AS highlights_text

            FROM tours t
            LEFT JOIN tour_categories tc ON t.category_id = tc.id
            WHERE t.id = ?
        """;

        // Query 1 phát ra dữ liệu phẳng
        jdbcTemplate.queryForObject(fetchSql, (rs, rowNum) -> {
            Long homestayId = rs.getLong("homestay_id");
            String categoryName = rs.getString("category_name");
            String tourName = rs.getString("name");
            double price = rs.getDouble("price_per_person");
            String durationType = rs.getString("duration_type");
            int durationValue = rs.getInt("duration_value");
            String highlights = rs.getString("highlights_text");

            // 2. Gom chữ cho AI "đọc" (Rất quan trọng để tìm kiếm ngữ nghĩa)
            String safeHighlights = (highlights != null) ? highlights : "";
            String safeCategory = (categoryName != null) ? categoryName : "Chung";

            String aiContextText = String.format("Tour %s. Thể loại: %s. Thời gian: %d %s. Trải nghiệm: %s",
                    tourName, safeCategory, durationValue, durationType, safeHighlights);

            // 3. Gọi Gemini nhúng Vector
            float[] embeddingArray = embeddingModel.embed(aiContextText);
            PGvector pgVector = new PGvector(embeddingArray);

            // 4. Lưu (Upsert) vào bảng tour_search_index
            String upsertSql = """
                INSERT INTO tour_search_index 
                (tour_id, homestay_id, category_name, name, price_per_person, duration_type, duration_value, highlights_text, embedding)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (tour_id) DO UPDATE SET
                category_name = EXCLUDED.category_name,
                name = EXCLUDED.name,
                price_per_person = EXCLUDED.price_per_person,
                duration_type = EXCLUDED.duration_type,
                duration_value = EXCLUDED.duration_value,
                highlights_text = EXCLUDED.highlights_text,
                embedding = EXCLUDED.embedding
            """;

            jdbcTemplate.update(upsertSql,
                    tourId, homestayId, categoryName, tourName, price,
                    durationType, durationValue, highlights, pgVector);

            return null;
        }, tourId);
    }
}