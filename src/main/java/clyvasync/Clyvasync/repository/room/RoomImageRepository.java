package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoomIdIn(List<Long> roomIds);
    List<RoomImage> findAllByRoomIdIn(List<Long> roomIds);
    List<RoomImage> findByRoomIdOrderByDisplayOrderAsc(Long roomId);

    List<RoomImage> findByRoomId(Long roomId);


    void deleteByRoomId(Long roomId);

    void deleteByRoomIdAndIdNotIn(Long roomId, List<Long> imageIds);
    List<RoomImage> findByImageUrlIn(List<String> imageUrls);
}
