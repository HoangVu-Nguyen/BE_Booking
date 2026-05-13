package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HomestayRoomRepository extends JpaRepository<HomestayRoom,Long> {
    List<HomestayRoom> findAllByHomestayId(Long homestayId);
}
