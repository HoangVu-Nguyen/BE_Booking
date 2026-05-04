package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity,Long> {
    @Query("SELECT a FROM Amenity a JOIN homestay_amenities ha ON a.id = ha.amenityId WHERE ha.homestayId = :homestayId")
    List<Amenity> findAllByHomestayId(@Param("homestayId") Long homestayId);}
