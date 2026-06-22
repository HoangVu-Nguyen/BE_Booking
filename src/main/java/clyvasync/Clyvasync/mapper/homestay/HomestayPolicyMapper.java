package clyvasync.Clyvasync.mapper.homestay;

import clyvasync.Clyvasync.dto.response.RoomPolicyResponse;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HomestayPolicyMapper {
    RoomPolicyResponse toRoomPolicyResponse(HomestayPolicy homestayPolicy);
    List<RoomPolicyResponse> toRoomPolicyResponseList(List<HomestayPolicy> homestayPolicies);
}
