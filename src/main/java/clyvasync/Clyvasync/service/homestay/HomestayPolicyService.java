package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.request.UpdateHomestayPolicyRequest;
import clyvasync.Clyvasync.dto.response.RoomPolicyResponse;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;

import java.util.List;

public interface HomestayPolicyService {
    HomestayPolicy getHomestayPolicyByHomestayId(Long homestayId);
    List<HomestayPolicy> findAllByHomestayId(Long homestayId);
    RoomPolicyResponse getPolicy(Long ownerId, Long homestayId);

    RoomPolicyResponse updatePolicy(
            Long ownerId,
            Long homestayId,
            UpdateHomestayPolicyRequest request
    );

}
