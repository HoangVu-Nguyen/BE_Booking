package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "review_images")
@Getter
@Setter
@NoArgsConstructor
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @Column(name = "review_id")
    private Long reviewId;
}