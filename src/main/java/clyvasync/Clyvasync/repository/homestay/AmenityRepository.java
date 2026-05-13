package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Amenity;
import clyvasync.Clyvasync.repository.projection.AmenityBatchProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    @Query("SELECT a FROM Amenity a " +
            "JOIN HomestayAmenity ha ON a.id = ha.amenityId " +
            "WHERE ha.homestayId = :homestayId")
    List<Amenity> findAllByHomestayId(@Param("homestayId") Long homestayId);
    @Query("SELECT ha.homestayId as homestayId, a as amenity " +
            "FROM Amenity a " +
            "JOIN HomestayAmenity ha ON a.id = ha.amenityId " +
            "WHERE ha.homestayId IN :homestayIds")
    List<AmenityBatchProjection> findAmenitiesByBatch(@Param("homestayIds") List<Long> homestayIds);
}