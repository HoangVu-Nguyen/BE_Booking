package clyvasync.Clyvasync.modules.homestay.entity;


import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.media.MediaStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "homestay_images")
@Getter @Setter
@Builder
@NoArgsConstructor

@AllArgsConstructor
public class HomestayImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id")
    private Long homestayId;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private MediaStatus status;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}