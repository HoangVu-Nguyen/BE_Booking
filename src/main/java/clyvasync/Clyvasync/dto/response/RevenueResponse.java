package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@ToString
public class RevenueResponse {
    private List<String> labels;
    private List<Double> revenue;
    private List<Double> gmv;
}