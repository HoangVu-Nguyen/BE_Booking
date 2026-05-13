package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RatePlanBenefitMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatePlanBenefitMappingRepository extends JpaRepository<RatePlanBenefitMapping, Long> {
    List<RatePlanBenefitMapping> findAllByRatePlanIdIn(List<Long> planIds);
    @Query("""
    SELECT m.ratePlanId, a.name 
    FROM RatePlanBenefitMapping m 
    JOIN Amenity a ON m.amenityId = a.id 
    WHERE m.ratePlanId IN :planIds
""")
    List<Object[]> findBenefitsByPlanIds(@Param("planIds") List<Long> planIds);
}