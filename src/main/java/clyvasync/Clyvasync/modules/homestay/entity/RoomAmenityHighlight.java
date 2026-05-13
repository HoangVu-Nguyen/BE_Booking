package clyvasync.Clyvasync.modules.homestay.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "room_amenity_highlights")
@Data @Builder
@NoArgsConstructor @AllArgsConstructor
@IdClass(RoomAmenityHighlightId.class) // Khai báo khóa chính tổ hợp
public class RoomAmenityHighlight {

    @Id
    @Column(name = "room_id")
    private Long roomId;

    @Id
    @Column(name = "amenity_id")
    private Integer amenityId;

    @Column(name = "display_value")
    private String displayValue; // '150 Mbps', 'King Size'
}

// File bổ trợ cho Khóa chính tổ hợp (Composite Key)
