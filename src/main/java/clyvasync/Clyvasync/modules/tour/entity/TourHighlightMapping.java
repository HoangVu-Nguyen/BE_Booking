package clyvasync.Clyvasync.modules.tour.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Table(name = "tour_highlight_mappings")
@Getter
@Setter
@IdClass(TourHighlightId.class)
public class TourHighlightMapping {

    @Id
    @Column(name = "tour_id")
    private Long tourId;

    @Id
    @Column(name = "highlight_id")
    private Integer highlightId;

    @Column(name = "custom_description", columnDefinition = "TEXT")
    private String customDescription;

    @Column(name = "is_included")
    private Boolean isIncluded = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;
}

