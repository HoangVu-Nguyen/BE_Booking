package clyvasync.Clyvasync.modules.room;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rate_plan_benefit_mapping")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(RatePlanBenefitId.class)
public class RatePlanBenefitMapping {

    @Id
    @Column(name = "rate_plan_id", nullable = false)
    private Long ratePlanId;

    @Id
    @Column(name = "amenity_id", nullable = false)
    private Integer amenityId;

    @Column(name = "display_value", length = 100)
    private String displayValue;
}