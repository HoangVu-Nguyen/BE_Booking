package clyvasync.Clyvasync.spec;


import clyvasync.Clyvasync.dto.request.GlobalSearchRequest;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

public class TourSearchSpec {

    public static Specification<Tour> buildGlobalSpec(GlobalSearchRequest filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. TÌM TỪ KHÓA (Tên Tour hoặc mô tả)
            if (StringUtils.hasText(filters.keyword())) {
                String searchPattern = "%" + filters.keyword().trim().toLowerCase() + "%";
                Predicate matchName = cb.like(cb.lower(cb.function("unaccent", String.class, root.get("name"))),
                        cb.function("unaccent", String.class, cb.literal(searchPattern)));
                predicates.add(matchName);
            }

            // 2. LỌC GIÁ (price_per_person nằm ngay trong bảng tours)
            if (filters.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerPerson"), filters.minPrice()));
            }
            if (filters.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerPerson"), filters.maxPrice()));
            }

            // 3. QUY MÔ (max_participants)
            if (filters.guests() != null && filters.guests() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxParticipants"), filters.guests()));
            }

            // 4. CATEGORY ID (Lọc trực tiếp)
            if (filters.category() != null && !filters.category().equals("ALL")) {
                // Tour Category ID thường là Integer, bác map category từ request sang ID ở đây
                // Nếu FE gửi lên "TOUR", bác check DB xem ID là mấy, ví dụ ID=2
                if (filters.category().equals("TOUR")) {
                    predicates.add(cb.equal(root.get("categoryId"), 2));
                }
            }

            // 5. TRẠNG THÁI
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), "ACTIVE"));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}