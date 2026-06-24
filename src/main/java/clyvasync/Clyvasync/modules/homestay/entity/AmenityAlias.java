package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Locale;

@Entity
@Table(
        name = "amenity_aliases",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_amenity_alias",
                        columnNames = {"amenity_id", "normalized_alias"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_amenity_aliases_normalized",
                        columnList = "normalized_alias"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amenity_id", nullable = false)
    private Integer amenityId;

    @Column(name = "alias", nullable = false, columnDefinition = "TEXT")
    private String alias;

    @Column(name = "normalized_alias", nullable = false, columnDefinition = "TEXT")
    private String normalizedAlias;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    @PreUpdate
    private void normalizeAlias() {
        if (alias != null) {
            this.normalizedAlias = alias
                    .trim()
                    .replaceAll("\\s+", " ")
                    .toLowerCase(Locale.ROOT);
        }
    }
}