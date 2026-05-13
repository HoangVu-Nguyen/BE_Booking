package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "homestay_amenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomestayAmenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "homestay_id")
    private Long homestayId;

    @Column(name = "amenity_id")
    private Integer amenityId;
}