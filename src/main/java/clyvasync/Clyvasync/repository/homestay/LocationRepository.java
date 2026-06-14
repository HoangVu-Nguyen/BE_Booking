package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository <Location,Integer> {
    List<Location> findAllByIdIn(List<Integer> ids);
    @Query("SELECT l.id FROM Location l WHERE LOWER(l.cityName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(l.slug) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Optional<Integer> findIdByNameOrSlug(@Param("keyword") String keyword);
}
