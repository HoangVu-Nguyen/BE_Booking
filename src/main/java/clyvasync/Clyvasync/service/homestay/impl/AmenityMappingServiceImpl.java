package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.record.AmenityMappingResult;
import clyvasync.Clyvasync.repository.homestay.AmenityAliasRepository;
import clyvasync.Clyvasync.service.homestay.AmenityMappingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class AmenityMappingServiceImpl  implements AmenityMappingService {
    private final AmenityAliasRepository amenityAliasRepository;
    private final Map<String, Integer> aliasCache = new ConcurrentHashMap<>();

    @Override
    public AmenityMappingResult processAiAmenities(List<String> aiKeywords) {
        if (aiKeywords == null || aiKeywords.isEmpty()) {
            return new AmenityMappingResult(new ArrayList<>(), "");
        }

        // Dùng Set để tự động loại bỏ ID trùng lặp (lỡ AI bóc ra "tv" và "tivi" cùng lúc)
        Set<Integer> matchedIds = new HashSet<>();
        StringBuilder unmappedWords = new StringBuilder();

        for (String word : aiKeywords) {
            String cleanWord = word.trim().toLowerCase();
            if (cleanWord.isEmpty()) continue;

            // 1. Kiểm tra xem từ này đã có trong Cache chưa
            Integer amenityId = aliasCache.get(cleanWord);

            // 2. Nếu chưa có, mới gọi xuống Database để tìm
            if (amenityId == null) {
                amenityId = amenityAliasRepository.findBestMatchAmenityId(cleanWord);

                // Nếu DB tìm thấy, lưu ngay vào Cache để lần sau dùng
                if (amenityId != null) {
                    aliasCache.put(cleanWord, amenityId);
                }
            }

            // 3. Phân loại kết quả (Vớt rác)
            if (amenityId != null) {
                matchedIds.add(amenityId); // Đưa vào danh sách lọc cứng
            } else {
                unmappedWords.append(word).append(" "); // Đưa vào danh sách Semantic
            }
        }

        return new AmenityMappingResult(new ArrayList<>(matchedIds), unmappedWords.toString().trim());
    }
}
