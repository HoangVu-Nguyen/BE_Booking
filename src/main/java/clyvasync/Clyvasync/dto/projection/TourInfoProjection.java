package clyvasync.Clyvasync.dto.projection;

public interface TourInfoProjection {
    String getTourName();
    java.time.LocalDate getTourDate();
    java.math.BigDecimal getPrice();
}