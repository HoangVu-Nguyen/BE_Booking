package clyvasync.Clyvasync.dto.projection;

public interface RevenueProjection {
    String getDay();
    Double getValue();
    String getTimeLabel(); // Label như "T1", "Q1", "2026"
    Double getRevenue();   // Doanh thu thuần
    Double getGmv();
}