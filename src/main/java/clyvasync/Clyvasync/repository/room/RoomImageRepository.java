package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoomIdIn(List<Long> roomIds);
}
