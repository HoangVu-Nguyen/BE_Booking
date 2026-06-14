package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.room.BedType;
import clyvasync.Clyvasync.modules.room.RoomBed;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BedResponse {
    private Long id;
    private BedType type;
    private Integer quantity;

}
