package clyvasync.Clyvasync.modules.room;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDate;

@Entity
@Table(
        name = "rate_plan_calendar",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rate_plan_calendar_rate_plan_night",
                        columnNames = {"rate_plan_id", "night_date"}
                )
        },
        indexes = {
                @Index(name = "idx_rate_plan_calendar_rate_plan_id", columnList = "rate_plan_id"),
                @Index(name = "idx_rate_plan_calendar_night_date", columnList = "night_date"),
                @Index(name = "idx_rate_plan_calendar_rate_plan_date", columnList = "rate_plan_id, night_date")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatePlanCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_plan_id", nullable = false)
    private Long ratePlanId;

    @Column(name = "night_date", nullable = false)
    private LocalDate nightDate;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}