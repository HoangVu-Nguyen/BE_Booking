package clyvasync.Clyvasync.modules.tour.entity;

import lombok.Data;

import java.io.Serializable;

@Data
class TourHighlightId implements Serializable {
    private Long tourId;
    private Integer highlightId;
}
