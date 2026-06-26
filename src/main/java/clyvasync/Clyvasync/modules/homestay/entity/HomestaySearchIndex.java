package clyvasync.Clyvasync.modules.homestay.entity;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

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

    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "max_guests")
    private Integer maxGuests;

    @Column(name = "bed_count")
    private Integer bedCount;

    @Column(name = "price_current")
    private BigDecimal priceCurrent;

    @Column(name = "average_rating")
    private BigDecimal averageRating;

    @Column(name = "review_count")
    private Integer reviewCount;

    /**
     * Dùng để filter chính xác tiện ích:
     * Có wifi    -> amenity_ids @> ARRAY[id_wifi]
     * Không wifi -> NOT (amenity_ids && ARRAY[id_wifi])
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "amenity_ids", columnDefinition = "integer[]")
    private Integer[] amenityIds;

    /**
     * Dùng cho PostgreSQL full-text search.
     */
//    @JdbcTypeCode(SqlTypes.OTHER)
//    @Column(name = "amenities_tsv", columnDefinition = "tsvector")
//    private String amenitiesTsv;


}