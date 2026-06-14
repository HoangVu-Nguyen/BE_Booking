package clyvasync.Clyvasync.dto.request;
import clyvasync.Clyvasync.enums.room.BedType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BedRequest {


    private Long id;

    private BedType type;

    private Integer quantity;
}