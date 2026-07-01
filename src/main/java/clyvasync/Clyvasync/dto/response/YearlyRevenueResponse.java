package clyvasync.Clyvasync.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record YearlyRevenueResponse(
        List<BigDecimal> thisYear,
        List<BigDecimal> lastYear
) {}
