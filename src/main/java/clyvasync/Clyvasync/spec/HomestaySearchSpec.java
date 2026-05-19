package clyvasync.Clyvasync.spec;

import clyvasync.Clyvasync.dto.request.GlobalSearchRequest;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayAmenity;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.homestay.entity.Location;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HomestaySearchSpec {

    public static Specification<Homestay> buildGlobalSpec(GlobalSearchRequest filters) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. TÌM TỪ KHÓA (Gần đúng)
            if (StringUtils.hasText(filters.keyword())) {
                String searchPattern = "%" + filters.keyword().trim().toLowerCase() + "%";

                Subquery<Integer> locSub = query.subquery(Integer.class);
                Root<Location> locRoot = locSub.from(Location.class);
                locSub.select(locRoot.get("id"));
                locSub.where(cb.like(cb.lower(cb.function("unaccent", String.class, locRoot.get("cityName"))), cb.function("unaccent", String.class, cb.literal(searchPattern))));

                Predicate matchLocation = root.get("locationId").in(locSub);
                Predicate matchName = cb.like(cb.lower(cb.function("unaccent", String.class, root.get("name"))), cb.function("unaccent", String.class, cb.literal(searchPattern)));

                predicates.add(cb.or(matchName, matchLocation));
            }

//            // 2. LỌC TIỆN ÍCH (Sử dụng EXISTS là chuẩn, giữ nguyên)
//            if (filters.amenityIds() != null && !filters.amenityIds().isEmpty()) {
//                for (Integer amId : filters.amenityIds()) {
//                    Subquery<Integer> amSub = query.subquery(Integer.class);
//                    Root<HomestayAmenity> amRoot = amSub.from(HomestayAmenity.class);
//                    amSub.select(cb.literal(1));
//                    amSub.where(
//                            cb.equal(amRoot.get("homestayId"), root.get("id")),
//                            cb.equal(amRoot.get("amenityId"), amId)
//                    );
//                    predicates.add(cb.exists(amSub));
//                }
//            }

//            // 3. LỌC GIÁ MAX (Chỉ lọc nếu có giá trị)
            if (filters.maxPrice() != null && filters.maxPrice().doubleValue() > 0) {
                Subquery<Long> roomSub = query.subquery(Long.class);
                Root<HomestayRoom> roomRoot = roomSub.from(HomestayRoom.class);
                roomSub.select(roomRoot.get("id"));
                roomSub.where(cb.equal(roomRoot.get("homestayId"), root.get("id")));

                Subquery<Integer> rateSub = query.subquery(Integer.class);
                Root<RoomRatePlan> rateRoot = rateSub.from(RoomRatePlan.class);
                rateSub.select(cb.literal(1));
                rateSub.where(
                        rateRoot.get("roomId").in(roomSub),
                        cb.lessThanOrEqualTo(rateRoot.get("price"), filters.maxPrice())
                );
                predicates.add(cb.exists(rateSub));
            }

            // 4. LỌC KHÁCH & PHÒNG (Sửa logic > 0 để tránh lọc mất dữ liệu)
            if ((filters.guests() != null && filters.guests() > 0) || (filters.bedrooms() != null && filters.bedrooms() > 0)) {
                Subquery<Integer> roomDetailSub = query.subquery(Integer.class);
                Root<HomestayRoom> detailRoot = roomDetailSub.from(HomestayRoom.class);
                roomDetailSub.select(cb.literal(1));

                List<Predicate> roomPreds = new ArrayList<>();
                roomPreds.add(cb.equal(detailRoot.get("homestayId"), root.get("id")));

                if (filters.guests() != null && filters.guests() > 0) {
                    roomPreds.add(cb.greaterThanOrEqualTo(detailRoot.get("maxGuests"), filters.guests()));
                }
                if (filters.bedrooms() != null && filters.bedrooms() > 0) {
                    roomPreds.add(cb.greaterThanOrEqualTo(detailRoot.get("bedCount"), filters.bedrooms()));
                }
                roomDetailSub.where(cb.and(roomPreds.toArray(new Predicate[0])));
                predicates.add(cb.exists(roomDetailSub));
            }

            // 5. RATING
            if (filters.minRating() != null && filters.minRating() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), filters.minRating()));
            }

            // 6. STATUS (Bỏ check deletedAt nếu DB không có, hoặc đảm bảo status là AVAILABLE)
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), "AVAILABLE"));

            // Thay đoạn log cũ bằng đoạn này
            log.info("DEBUG: Generated predicates count: {}", predicates.size());
            for (int i = 0; i < predicates.size(); i++) {
                log.info("DEBUG: Predicate [{}]: {}", i, predicates.get(i).toString());
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}