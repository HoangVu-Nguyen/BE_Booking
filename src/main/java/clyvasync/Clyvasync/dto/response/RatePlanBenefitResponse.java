package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RatePlanBenefitResponse {

    private Long ratePlanId;

    private Integer amenityId;

    private String name;

    private String iconName;

    private String groupName;

    private String displayValue;
}