package clyvasync.Clyvasync.modules.homestay.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "homestay_images")
@Getter
@Setter
@NoArgsConstructor
public class HomestayImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id", nullable = false)
    private Long homestayId;

    private String imageUrl;

    private Boolean isPrimary = false;

    private Integer displayOrder = 0;
}