package clyvasync.Clyvasync.modules.tour.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tour_highlights_catalog")
@Getter
@Setter
public class TourHighlightCatalog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "icon_key", length = 50)
    private String iconKey;

    @Column(name = "default_description", columnDefinition = "TEXT")
    private String defaultDescription;
}