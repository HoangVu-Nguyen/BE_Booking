package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    @Query(value = "SELECT a.* FROM amenities a " +
            "JOIN homestay_amenities ha ON a.id = ha.amenity_id " +
            "WHERE ha.homestay_id = :homestayId",
            nativeQuery = true)
    List<Amenity> findAllByHomestayId(@Param("homestayId") Long homestayId);
}