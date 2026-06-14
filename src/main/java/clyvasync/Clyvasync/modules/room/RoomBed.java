package clyvasync.Clyvasync.modules.room;


import clyvasync.Clyvasync.enums.room.BedType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
@Entity
@Table(name = "room_beds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomBed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type", nullable = false, length = 50)
    private BedType bedType;

    @Column(name = "quantity")
    private Integer quantity = 1;
}