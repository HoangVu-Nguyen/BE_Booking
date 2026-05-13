package clyvasync.Clyvasync.modules.room;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rate_plan_benefit_mapping")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
@IdClass(RatePlanBenefitId.class)
public class RatePlanBenefitMapping {

    @Id
    @Column(name = "rate_plan_id")
    private Long ratePlanId;

    @Id
    @Column(name = "amenity_id")
    private Integer amenityId;
}

