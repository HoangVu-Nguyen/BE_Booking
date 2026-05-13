package clyvasync.Clyvasync.modules.room;
import jakarta.persistence.*;
import lombok.*;

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

