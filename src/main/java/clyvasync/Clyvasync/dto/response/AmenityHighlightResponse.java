package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AmenityHighlightResponse {
    private Long roomId;
    private String name;
    private String icon;
    private String displayValue;
}