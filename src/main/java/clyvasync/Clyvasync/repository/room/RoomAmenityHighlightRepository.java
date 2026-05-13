package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.modules.room.RoomAmenityHighlight;
import clyvasync.Clyvasync.modules.room.RoomAmenityHighlightId; // Import cái ID class
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

// Phải dùng RoomAmenityHighlightId ở đây thay vì Long
public interface RoomAmenityHighlightRepository extends JpaRepository<RoomAmenityHighlight, RoomAmenityHighlightId> {

    List<RoomAmenityHighlight> findAllByRoomIdIn(List<Long> roomIds);

    @Query("""
        SELECT new clyvasync.Clyvasync.dto.response.AmenityHighlightResponse(
            h.roomId, a.iconName, a.name, h.displayValue
        )
        FROM RoomAmenityHighlight h
        JOIN Amenity a ON h.amenityId = a.id
        WHERE h.roomId IN :roomIds
    """)
    List<AmenityHighlightResponse> findAllHighlightsByRoomIds(@Param("roomIds") List<Long> roomIds);
    @Query("""
    SELECT rah.roomId, a.iconName, a.name, rah.displayValue 
    FROM RoomAmenityHighlight rah 
    JOIN Amenity a ON rah.amenityId = a.id 
    WHERE rah.roomId IN :roomIds
""")
    List<Object[]> findHighlightsByRoomIds(@Param("roomIds") List<Long> roomIds);
}