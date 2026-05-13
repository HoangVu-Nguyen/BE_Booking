package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public  class AmenityHighlightResponse {
    private Long roomId;
    private String icon;        // 'wifi', 'king_bed'
    private String label;       // 'Connectivity', 'Bedding'
    private String value;       // '150 Mbps', 'Ultra King'
}