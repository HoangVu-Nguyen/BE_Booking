package clyvasync.Clyvasync.dto.detail;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class PropertyDetailInfo {
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private List<String> images; // Bộ sưu tập ảnh để làm cái scroll ngang
}
