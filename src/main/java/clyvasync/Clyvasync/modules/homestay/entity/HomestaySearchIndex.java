package modules.homestay.entity;

import jakarta.persistence.*;
import lombok.*;
import com.pgvector.PGvector;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "homestay_search_index")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomestaySearchIndex {

    @Id
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "homestay_id")
    private Long homestayId;

    private String name;
    private String city;
    private Integer maxGuests;
    private Integer bedCount;
    private Double priceCurrent;
    private Double averageRating;
    private Integer reviewCount;
    @Column(name = "embedding", columnDefinition = "vector(1536)")
    @JdbcTypeCode(SqlTypes.OTHER)
    private PGvector embedding;
}