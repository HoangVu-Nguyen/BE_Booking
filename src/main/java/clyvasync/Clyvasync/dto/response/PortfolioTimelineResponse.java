package clyvasync.Clyvasync.dto.response;

import java.util.List;

public record PortfolioTimelineResponse(
        List<HomeTimelineResponse> homes // Danh sách tất cả các Home của chủ đó
) {}
