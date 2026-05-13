package clyvasync.Clyvasync.modules.homestay.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "room_rate_plans")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RoomRatePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId; // Link tới xác phòng bằng ID

    private String name;
    private BigDecimal price;

    @Column(name = "is_non_refundable")
    private Boolean isNonRefundable;
}