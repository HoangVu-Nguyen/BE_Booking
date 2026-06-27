package clyvasync.Clyvasync.mapper.kyc;

import clyvasync.Clyvasync.dto.response.HostKycProfileResponse;
import clyvasync.Clyvasync.modules.kyc.entity.HostKycProfile;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HostKycMapper {
    HostKycProfileResponse toHostKycProfileResponse(HostKycProfile hostKycProfile);
    List<HostKycProfileResponse> toHostKycProfileResponseList(List<HostKycProfile> hostKycProfiles);
}
