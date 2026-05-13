package clyvasync.Clyvasync.modules.homestay.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class RatePlanBenefitId implements java.io.Serializable {
    private Long ratePlanId;
    private Integer amenityId;
}