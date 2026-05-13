package clyvasync.Clyvasync.modules.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tour_categories")
@Getter
@Setter
public class TourCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "icon_url", columnDefinition = "TEXT")
    private String iconUrl;
}