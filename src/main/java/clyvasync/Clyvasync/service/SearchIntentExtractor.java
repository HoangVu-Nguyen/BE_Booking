package clyvasync.Clyvasync.service;

import clyvasync.Clyvasync.dto.record.SearchIntent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchIntentExtractor {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${spring.ai.openai.api-key}")
    private String geminiApiKey;

    public SearchIntent extract(String userQuery) {
        try {
            String catalog = loadAmenityCatalog();
            String prompt = buildPrompt(catalog, userQuery);

            String content = callGemini(prompt);
            String cleanJson = cleanJson(content);

            SearchIntent intent = objectMapper.readValue(cleanJson, SearchIntent.class);
            return sanitize(intent, userQuery);

        } catch (Exception e) {
            SearchIntent fallbackIntent = fallback(userQuery);

            // Cực kỳ quan trọng:
            // Nếu AI lỗi, vẫn dùng DB aliases để bắt các câu như "không có wifi".
            return enrichAmenityIntentByDbAliases(fallbackIntent, userQuery);
        }
    }

    private String buildPrompt(String catalog, String userQuery) {
        return """
            Bạn là bộ phân tích câu tìm kiếm homestay.

            Nhiệm vụ:
            - Chỉ trả về JSON hợp lệ.
            - Không markdown.
            - Không giải thích.
            - Không tự bịa amenity id.
            - Chỉ dùng id có trong danh sách tiện ích.
            - Nếu người dùng nói cần/có/phải có một tiện ích, đưa id vào mustHaveAmenityIds.
            - Nếu người dùng nói không có/không cần/trừ/đừng lấy tiện ích đó, đưa id vào mustNotHaveAmenityIds.
            - Nếu người dùng nói "wifi không quan trọng", "có cũng được không có cũng được", không đưa vào have/notHave.
            - Nếu người dùng nói "không quá 1 triệu", đó là maxPrice, không phải phủ định tiện ích.
            - Nếu người dùng nói "dưới X", "không quá X", "tối đa X", đặt maxPrice = X.
            - Nếu người dùng nói "trên X", "từ X trở lên", đặt minPrice = X.
            - Nếu người dùng nói "từ X đến Y", đặt minPrice = X và maxPrice = Y.
            - Nếu người dùng nói "tầm X", "khoảng X", "cỡ X", "quanh X", đặt targetPrice = X,
              minPrice = X * 0.8, maxPrice = X * 1.2, sortBy = "PRICE_NEAR".
            - Nếu người dùng nói "rẻ nhất", sortBy = "PRICE_ASC".
            - Nếu người dùng nói "đánh giá cao", "rating cao", sortBy = "RATING_DESC".
            - semanticQuery là phần còn lại để search ngữ nghĩa.

            Quy đổi giá:
            - 800k = 800000
            - 800 nghìn = 800000
            - 1 triệu = 1000000
            - 1tr5 = 1500000
            - 2 triệu = 2000000

            Danh sách tiện ích:
            %s

            Câu hỏi người dùng:
            "%s"

            Schema JSON:
            {
              "semanticQuery": "string",
              "mustHaveAmenityIds": [],
              "mustNotHaveAmenityIds": [],
              "maxGuests": null,
              "minBeds": null,
              "targetPrice": null,
              "maxPrice": null,
              "minPrice": null,
              "city": null,
              "sortBy": "RELEVANCE"
            }
            """.formatted(catalog, userQuery);
    }

    private String callGemini(String prompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key="
                + geminiApiKey;

        GeminiRequest request = new GeminiRequest(
                List.of(
                        new Content(
                                List.of(new Part(prompt))
                        )
                )
        );

        JsonNode response = restTemplate.postForObject(url, request, JsonNode.class);

        if (response == null) {
            throw new RuntimeException("Gemini response is null");
        }

        JsonNode textNode = response
                .path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text");

        if (textNode.isMissingNode() || textNode.asText().isBlank()) {
            throw new RuntimeException("Gemini response text is empty: " + response);
        }

        return textNode.asText();
    }

    private String cleanJson(String content) {
        if (content == null) {
            return "{}";
        }

        String cleaned = content
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start >= 0 && end > start) {
            return cleaned.substring(start, end + 1);
        }

        return cleaned;
    }

    private String loadAmenityCatalog() {
        String sql = """
            SELECT
                a.id,
                a.name,
                a.group_name,
                COALESCE(string_agg(aa.alias, ', '), '') AS aliases
            FROM amenities a
            LEFT JOIN amenity_aliases aa ON aa.amenity_id = a.id
            GROUP BY a.id, a.name, a.group_name
            ORDER BY a.id
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                "- id=" + rs.getInt("id")
                        + ", name=" + rs.getString("name")
                        + ", group=" + rs.getString("group_name")
                        + ", aliases=" + rs.getString("aliases")
        ).stream().collect(Collectors.joining("\n"));
    }

    private SearchIntent sanitize(SearchIntent intent, String originalQuery) {
        Set<Integer> validAmenityIds = new HashSet<>(
                jdbcTemplate.queryForList("SELECT id FROM amenities", Integer.class)
        );

        List<Integer> mustHave = safeList(intent.mustHaveAmenityIds())
                .stream()
                .filter(validAmenityIds::contains)
                .distinct()
                .toList();

        List<Integer> mustNotHave = safeList(intent.mustNotHaveAmenityIds())
                .stream()
                .filter(validAmenityIds::contains)
                .distinct()
                .toList();

        Set<Integer> notHaveSet = new HashSet<>(mustNotHave);

        mustHave = mustHave.stream()
                .filter(id -> !notHaveSet.contains(id))
                .toList();

        String semanticQuery = intent.semanticQuery();
        if (semanticQuery == null || semanticQuery.isBlank()) {
            semanticQuery = originalQuery;
        }

        String sortBy = intent.sortBy();
        if (sortBy == null || sortBy.isBlank()) {
            sortBy = "RELEVANCE";
        }

        SearchIntent sanitized = new SearchIntent(
                semanticQuery,
                mustHave,
                mustNotHave,
                intent.maxGuests(),
                intent.minBeds(),
                intent.targetPrice(),
                intent.maxPrice(),
                intent.minPrice(),
                intent.city(),
                sortBy
        );

        return enrichAmenityIntentByDbAliases(sanitized, originalQuery);
    }

    private SearchIntent fallback(String query) {
        return new SearchIntent(
                query,
                List.of(),
                List.of(),
                null,
                null,
                null,
                null,
                null,
                null,
                "RELEVANCE"
        );
    }

    private SearchIntent enrichAmenityIntentByDbAliases(SearchIntent intent, String originalQuery) {
        if (originalQuery == null || originalQuery.isBlank()) {
            return intent;
        }

        String normalizedQuery = normalizeText(originalQuery);
        List<AmenityTerm> terms = loadAmenityTerms();

        LinkedHashSet<Integer> mustHave = new LinkedHashSet<>(safeList(intent.mustHaveAmenityIds()));
        LinkedHashSet<Integer> mustNotHave = new LinkedHashSet<>(safeList(intent.mustNotHaveAmenityIds()));

        for (AmenityTerm term : terms) {
            String normalizedTerm = normalizeText(term.term());

            if (normalizedTerm.isBlank()) {
                continue;
            }

            if (!normalizedQuery.contains(normalizedTerm)) {
                continue;
            }

            if (isNeutralAmenityMention(normalizedQuery, normalizedTerm)) {
                mustHave.remove(term.amenityId());
                mustNotHave.remove(term.amenityId());
                continue;
            }

            if (isNegativeAmenityMention(normalizedQuery, normalizedTerm)) {
                mustNotHave.add(term.amenityId());
                mustHave.remove(term.amenityId());
                continue;
            }

            if (isPositiveAmenityMention(normalizedQuery, normalizedTerm)) {
                if (!mustNotHave.contains(term.amenityId())) {
                    mustHave.add(term.amenityId());
                }
            }
        }

        return new SearchIntent(
                intent.semanticQuery(),
                new ArrayList<>(mustHave),
                new ArrayList<>(mustNotHave),
                intent.maxGuests(),
                intent.minBeds(),
                intent.targetPrice(),
                intent.maxPrice(),
                intent.minPrice(),
                intent.city(),
                intent.sortBy()
        );
    }

    private List<AmenityTerm> loadAmenityTerms() {
        String sql = """
            SELECT id AS amenity_id, name AS term
            FROM amenities

            UNION

            SELECT amenity_id, alias AS term
            FROM amenity_aliases
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new AmenityTerm(
                        rs.getInt("amenity_id"),
                        rs.getString("term")
                )
        );
    }

    private boolean isNegativeAmenityMention(String query, String term) {
        String quotedTerm = Pattern.quote(term);

        List<String> patterns = List.of(
                "(khong co|ko co|k co|khong can|khong muon|dung lay|bo qua|tru|ngoai tru)\\s+.{0,20}" + quotedTerm,
                quotedTerm + "\\s+.{0,20}(thi\\s+)?(khong can|khong muon|khong quan trong)"
        );

        return patterns.stream()
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(query).find());
    }

    private boolean isPositiveAmenityMention(String query, String term) {
        String quotedTerm = Pattern.quote(term);

        List<String> patterns = List.of(
                "(co|can|phai co|bat buoc co|mien co|yeu cau co)\\s+.{0,20}" + quotedTerm,
                quotedTerm + "\\s+.{0,20}(la duoc|cang tot)"
        );

        return patterns.stream()
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(query).find());
    }

    private boolean isNeutralAmenityMention(String query, String term) {
        String quotedTerm = Pattern.quote(term);

        List<String> patterns = List.of(
                quotedTerm + "\\s+.{0,20}(khong quan trong|sao cung duoc)",
                "(co cung duoc khong co cung duoc|khong nhat thiet phai co)\\s+.{0,20}" + quotedTerm
        );

        return patterns.stream()
                .map(Pattern::compile)
                .anyMatch(pattern -> pattern.matcher(query).find());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace("đ", "d")
                .replace("Đ", "D")
                .toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }

    private List<Integer> safeList(List<Integer> values) {
        return values == null ? List.of() : values;
    }

    private record GeminiRequest(List<Content> contents) {}

    private record Content(List<Part> parts) {}

    private record Part(String text) {}

    private record AmenityTerm(Integer amenityId, String term) {}
}