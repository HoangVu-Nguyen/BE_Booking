package clyvasync.Clyvasync.mapper.room;

import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    RoomResponse toRoomResponse(HomestayRoom room);
}
