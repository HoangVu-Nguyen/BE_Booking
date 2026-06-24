package clyvasync.Clyvasync.service.ai.impl;

import clyvasync.Clyvasync.dto.record.SearchIntent;
import clyvasync.Clyvasync.dto.response.HomestaySearchResultResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.service.SearchIntentExtractor;
import clyvasync.Clyvasync.service.ai.SearchSyncService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import com.pgvector.PGvector;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ai.embedding.EmbeddingModel;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchSyncServiceImpl implements SearchSyncService {

    private final JdbcTemplate jdbcTemplate;
    private final EmbeddingModel embeddingModel;
    private final HomestayService homestayService;
    private final SearchIntentExtractor searchIntentExtractor;

    public SearchSyncServiceImpl(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel, @Lazy HomestayService homestayService,SearchIntentExtractor searchIntentExtractor) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddingModel = embeddingModel;
        this.homestayService = homestayService;
        this.searchIntentExtractor = searchIntentExtractor;
    }

    @Override
    @Transactional
    public void syncRoomToIndex(
            Long roomId,
            Long homestayId,
            String homestayName,
            String city,
            int beds,
            int guests,
            double price,
            String allAmenitiesText
    ) {
        syncRoomToIndexInternal(
                roomId,
                homestayId,
                homestayName,
                city,
                beds,
                guests,
                BigDecimal.valueOf(price),
                BigDecimal.ZERO,
                0,
                List.of(),
                allAmenitiesText
        );
    }
    private void syncRoomToIndexInternal(
            Long roomId,
            Long homestayId,
            String homestayName,
            String city,
            int beds,
            int guests,
            BigDecimal price,
            BigDecimal averageRating,
            Integer reviewCount,
            List<Integer> amenityIds,
            String allAmenitiesText
    ) {
        String safeAmenities = allAmenitiesText != null ? allAmenitiesText : "";
        String safeCity = city != null ? city : "";

        String textToEmbed = "Homestay " + homestayName
                + " tại " + safeCity
                + ". Số giường: " + beds
                + ". Số khách tối đa: " + guests
                + ". Giá: " + price
                + ". Tiện ích: " + safeAmenities;

        float[] embeddingArray = embeddingModel.embed(textToEmbed);
        PGvector pgVector = new PGvector(embeddingArray);

        String sql = """
        INSERT INTO homestay_search_index
        (
            room_id,
            homestay_id,
            name,
            city,
            bed_count,
            max_guests,
            price_current,
            average_rating,
            review_count,
            amenity_ids,
            amenities_tsv,
            embedding
        )
        VALUES
        (
            ?, ?, ?, ?, ?, ?, ?, ?, ?,
            ?::integer[],
            to_tsvector('simple', COALESCE(?, '')),
            ?
        )
        ON CONFLICT (room_id) DO UPDATE SET
            homestay_id = EXCLUDED.homestay_id,
            name = EXCLUDED.name,
            city = EXCLUDED.city,
            bed_count = EXCLUDED.bed_count,
            max_guests = EXCLUDED.max_guests,
            price_current = EXCLUDED.price_current,
            average_rating = EXCLUDED.average_rating,
            review_count = EXCLUDED.review_count,
            amenity_ids = EXCLUDED.amenity_ids,
            amenities_tsv = EXCLUDED.amenities_tsv,
            embedding = EXCLUDED.embedding
    """;

        jdbcTemplate.update(
                sql,
                roomId,
                homestayId,
                homestayName,
                safeCity,
                beds,
                guests,
                price,
                averageRating == null ? BigDecimal.ZERO : averageRating,
                reviewCount == null ? 0 : reviewCount,
                toPgIntArrayLiteral(amenityIds),
                safeAmenities,
                pgVector
        );
    }
        @Override
        public void triggerSyncForRoom(Long roomId) {
            String fetchSql = """
            SELECT 
                r.id AS room_id, 
                r.homestay_id, 
                h.name AS homestay_name,
                COALESCE((SELECT l.city_name FROM locations l WHERE l.id = h.location_id), h.address_detail) AS city,
                r.name AS room_name, 
                COALESCE(r.bed_count, 0) AS bed_count,
                COALESCE(r.max_guests, 0) AS max_guests,
                COALESCE(
                    (SELECT array_agg(DISTINCT x.amenity_id) FROM (
                        SELECT rah.amenity_id FROM room_amenity_highlights rah WHERE rah.room_id = r.id
                        UNION
                        SELECT rpbm.amenity_id FROM rate_plan_benefit_mapping rpbm 
                        JOIN room_rate_plans rrp ON rpbm.rate_plan_id = rrp.id WHERE rrp.room_id = r.id
                    ) x),
                    ARRAY[]::integer[]
                ) AS amenity_ids,
                COALESCE((SELECT MIN(price) FROM room_rate_plans WHERE room_id = r.id), 0) AS min_price,
                (SELECT string_agg(quantity || ' ' || bed_type, ', ') FROM room_beds WHERE room_id = r.id) AS beds_text,
                (SELECT string_agg(a.name || COALESCE(' (' || rah.display_value || ')', ''), ', ') 
                 FROM room_amenity_highlights rah JOIN amenities a ON rah.amenity_id = a.id WHERE rah.room_id = r.id) AS room_amenities,
                (SELECT string_agg(DISTINCT a.name, ', ') FROM rate_plan_benefit_mapping rpbm
                 JOIN room_rate_plans rrp ON rpbm.rate_plan_id = rrp.id JOIN amenities a ON rpbm.amenity_id = a.id WHERE rrp.room_id = r.id) AS plan_benefits
            FROM homestay_rooms r
            JOIN homestays h ON r.homestay_id = h.id
            WHERE r.id = ?
        """;

            Map<String, Object> data;
            try {
                // 1. Lấy dữ liệu (Chỉ query, giải phóng DB connection ngay lập tức)
                data = jdbcTemplate.queryForMap(fetchSql, roomId);
            } catch (EmptyResultDataAccessException e) {
                // Nếu không tìm thấy phòng (bị xóa cứng) -> Xóa index
                deleteRoomIndex(roomId);
                return;
            }

            // 2. Trích xuất dữ liệu
            Long homestayId = ((Number) data.get("homestay_id")).longValue();
            String homestayName = (String) data.get("homestay_name");
            String roomName = (String) data.get("room_name");
            String city = (String) data.get("city");
            int bedCount = ((Number) data.get("bed_count")).intValue();
            int maxGuests = ((Number) data.get("max_guests")).intValue();
            BigDecimal price = toBigDecimal(data.get("min_price"));
            List<Integer> amenityIds = extractIntegerArray(data.get("amenity_ids"));

            String bedsText = (String) data.get("beds_text");
            String roomAmenities = (String) data.get("room_amenities");
            String planBenefits = (String) data.get("plan_benefits");

            String safeCity = city != null ? city : "";
            String safeAmenities = roomAmenities != null ? roomAmenities : "";

            // 3. Xây dựng văn bản mồi AI (Narrative format)
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("Phòng ").append(roomName)
                    .append(" thuộc homestay ").append(homestayName)
                    .append(", tọa lạc tại ").append(safeCity).append(". ")
                    .append("Sức chứa tối đa ").append(maxGuests).append(" khách ")
                    .append("với ").append(bedCount).append(" giường. ")
                    .append("Mức giá thấp nhất từ ").append(price).append(" VND. ");

            if (bedsText != null) contextBuilder.append("Cấu trúc giường: ").append(bedsText).append(". ");
            if (roomAmenities != null) contextBuilder.append("Tiện ích nổi bật: ").append(safeAmenities).append(". ");
            if (planBenefits != null) contextBuilder.append("Quyền lợi đi kèm: ").append(planBenefits).append(". ");

            String finalContext = contextBuilder.toString();

            // 4. GỌI API EMBEDDING (An toàn: Không có Transaction nào đang mở)
            float[] embeddingArray = embeddingModel.embed(finalContext);

            // 5. Lưu xuống DB
            // LƯU Ý: Để @Transactional ở hàm saveToIndex hoạt động khi gọi trong cùng 1 class,
            // bạn có thể cần tách hàm này sang một class khác (ví dụ: HomestayIndexRepository),
            // hoặc cứ để jdbcTemplate tự quản lý auto-commit (với 1 câu lệnh SQL thì vẫn an toàn).
            saveToIndex(
                    roomId, homestayId, homestayName + " - " + roomName, safeCity,
                    bedCount, maxGuests, price, BigDecimal.ZERO, 0,
                    amenityIds, safeAmenities, embeddingArray
            );
        }
        @Transactional
        public void saveToIndex(
                Long roomId, Long homestayId, String fullName, String city,
                int bedCount, int maxGuests, BigDecimal priceCurrent,
                BigDecimal averageRating, Integer reviewCount,
                List<Integer> amenityIds, String amenitiesTsv, float[] embeddingArray
        ) {
            PGvector pgVector = new PGvector(embeddingArray);

            String sql = """
            INSERT INTO homestay_search_index
            (
                room_id, homestay_id, name, city, bed_count, max_guests, 
                price_current, average_rating, review_count, amenity_ids, 
                amenities_tsv, embedding
            )
            VALUES
            (
                ?, ?, ?, ?, ?, ?, ?, ?, ?,
                ?::integer[],
                to_tsvector('simple', COALESCE(?, '')),
                ?
            )
            ON CONFLICT (room_id) DO UPDATE SET
                homestay_id = EXCLUDED.homestay_id,
                name = EXCLUDED.name,
                city = EXCLUDED.city,
                bed_count = EXCLUDED.bed_count,
                max_guests = EXCLUDED.max_guests,
                price_current = EXCLUDED.price_current,
                average_rating = EXCLUDED.average_rating,
                review_count = EXCLUDED.review_count,
                amenity_ids = EXCLUDED.amenity_ids,
                amenities_tsv = EXCLUDED.amenities_tsv,
                embedding = EXCLUDED.embedding
        """;

            jdbcTemplate.update(
                    sql,
                    roomId, homestayId, fullName, city, bedCount, maxGuests,
                    priceCurrent, averageRating, reviewCount,
                    toPgIntArrayLiteral(amenityIds), amenitiesTsv, pgVector
            );
        }

        /**
         * Xóa Index khi phòng không còn tồn tại
         */
        @Transactional
        public void deleteRoomIndex(Long roomId) {
            jdbcTemplate.update("DELETE FROM homestay_search_index WHERE room_id = ?", roomId);
        }
    @Override
    public List<HomestaySearchResultResponse> hybridSearch(String query, int limit) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        int safeLimit = Math.max(1, Math.min(limit, 50));

        SearchIntent intent = searchIntentExtractor.extract(query);
        System.out.println("Intent: " + intent);

        String semanticQuery = intent.semanticQuery() != null && !intent.semanticQuery().isBlank()
                ? intent.semanticQuery().trim()
                : query.trim();

        float[] queryVector = embeddingModel.embed(semanticQuery);
        PGvector pgVector = new PGvector(queryVector);

        StringBuilder sql = new StringBuilder("""
        SELECT *
        FROM (
            SELECT
                *,
                (embedding <=> ?::vector) AS vector_distance,
                COALESCE(
                    ts_rank(
                        COALESCE(amenities_tsv, to_tsvector('simple', '')),
                        plainto_tsquery('simple', ?)
                    ),
                    0
                ) AS rank_score
            FROM homestay_search_index
            WHERE embedding IS NOT NULL
    """);

        List<Object> params = new ArrayList<>();
        params.add(pgVector);
        params.add(semanticQuery);

        if (intent.city() != null && !intent.city().isBlank()) {
            sql.append(" AND LOWER(city) LIKE LOWER(?) ");
            params.add("%" + intent.city().trim() + "%");
        }

        if (intent.maxGuests() != null) {
            sql.append(" AND max_guests >= ? ");
            params.add(intent.maxGuests());
        }

        if (intent.minBeds() != null) {
            sql.append(" AND bed_count >= ? ");
            params.add(intent.minBeds());
        }

        BigDecimal minPrice = intent.minPrice();
        BigDecimal maxPrice = intent.maxPrice();

        if (intent.targetPrice() != null) {
            BigDecimal targetPrice = intent.targetPrice();

            if (minPrice == null) {
                minPrice = targetPrice.multiply(BigDecimal.valueOf(0.8));
            }

            if (maxPrice == null) {
                maxPrice = targetPrice.multiply(BigDecimal.valueOf(1.2));
            }
        }

        if (minPrice != null) {
            sql.append(" AND price_current >= ? ");
            params.add(minPrice);
        }

        if (maxPrice != null) {
            sql.append(" AND price_current <= ? ");
            params.add(maxPrice);
        }

        if (intent.mustHaveAmenityIds() != null && !intent.mustHaveAmenityIds().isEmpty()) {
            sql.append(" AND COALESCE(amenity_ids, ARRAY[]::integer[]) @> ?::integer[] ");
            params.add(toPgIntArrayLiteral(intent.mustHaveAmenityIds()));
        }

        if (intent.mustNotHaveAmenityIds() != null && !intent.mustNotHaveAmenityIds().isEmpty()) {
            sql.append(" AND NOT (COALESCE(amenity_ids, ARRAY[]::integer[]) && ?::integer[]) ");
            params.add(toPgIntArrayLiteral(intent.mustNotHaveAmenityIds()));
        }

        sql.append("""
            ) AS sub
            ORDER BY
                CASE
                    WHEN ? = 'PRICE_NEAR' THEN ABS(price_current - ?::numeric)
                    ELSE NULL
                END ASC NULLS LAST,

                CASE
                    WHEN ? = 'PRICE_ASC' THEN price_current
                    ELSE NULL
                END ASC NULLS LAST,

                CASE
                    WHEN ? = 'PRICE_DESC' THEN price_current
                    ELSE NULL
                END DESC NULLS LAST,

                CASE
                    WHEN ? = 'RATING_DESC' THEN average_rating
                    ELSE NULL
                END DESC NULLS LAST,

                (
                    vector_distance * 0.65
                    + (1.0 / (rank_score + 1)) * 0.35
                ) ASC
            LIMIT ?
    """);

        String sortBy = normalizeSortBy(intent.sortBy(), intent.targetPrice());
        BigDecimal targetPriceForSort = intent.targetPrice() != null
                ? intent.targetPrice()
                : BigDecimal.ZERO;

        params.add(sortBy);
        params.add(targetPriceForSort);

        params.add(sortBy);
        params.add(sortBy);
        params.add(sortBy);

        params.add(safeLimit);

        return jdbcTemplate.query(
                sql.toString(),
                params.toArray(),
                (rs, rowNum) -> homestayService.mapToHomestay(rs)
        );
    }
    private String normalizeSortBy(String sortBy, BigDecimal targetPrice) {
        if (sortBy == null || sortBy.isBlank()) {
            return "RELEVANCE";
        }

        String normalized = sortBy.trim().toUpperCase();

        if ("PRICE_NEAR".equals(normalized) && targetPrice == null) {
            return "RELEVANCE";
        }

        return switch (normalized) {
            case "PRICE_NEAR", "PRICE_ASC", "PRICE_DESC", "RATING_DESC" -> normalized;
            default -> "RELEVANCE";
        };
    }

    private String toPgIntArrayLiteral(Collection<Integer> values) {
        if (values == null || values.isEmpty()) {
            return "{}";
        }

        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "{", "}"));
    }
    private List<Integer> extractIntegerArray(Object value) {
        if (value == null) {
            return List.of();
        }

        try {
            if (value instanceof java.sql.Array sqlArray) {
                Object array = sqlArray.getArray();

                if (array instanceof Integer[] integers) {
                    return Arrays.stream(integers)
                            .filter(Objects::nonNull)
                            .distinct()
                            .toList();
                }

                if (array instanceof Number[] numbers) {
                    return Arrays.stream(numbers)
                            .filter(Objects::nonNull)
                            .map(Number::intValue)
                            .distinct()
                            .toList();
                }

                if (array instanceof Object[] objects) {
                    return Arrays.stream(objects)
                            .filter(Objects::nonNull)
                            .map(item -> ((Number) item).intValue())
                            .distinct()
                            .toList();
                }
            }

            if (value instanceof Object[] objects) {
                return Arrays.stream(objects)
                        .filter(Objects::nonNull)
                        .map(item -> ((Number) item).intValue())
                        .distinct()
                        .toList();
            }

        } catch (Exception e) {
            throw new IllegalArgumentException("Không parse được amenity_ids: " + value, e);
        }

        return List.of();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }

        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }

        return new BigDecimal(value.toString());
    }
}