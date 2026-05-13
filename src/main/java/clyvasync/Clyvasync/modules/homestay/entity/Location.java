package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "locations")
@Getter
@Setter
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "city_name", nullable = false, length = 100)
    private String cityName;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "is_popular")
    private Boolean isPopular = false;


}