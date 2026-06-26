package clyvasync.Clyvasync.tool.criteria;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record HomeSearchCriteria (
    @JsonPropertyDescription("Thành phố/địa danh. VD: Đà Lạt")
    String location,

    @JsonPropertyDescription("Ngày nhận phòng (YYYY-MM-DD)")
    String checkInDate,

    @JsonPropertyDescription("Ngày trả phòng (YYYY-MM-DD)")
    String checkOutDate,

    @JsonPropertyDescription("Số lượng khách")
    Integer guests,

    @JsonPropertyDescription("Số giường/phòng ngủ")
    Integer bedCount,

    @JsonPropertyDescription("Giá tối thiểu (VNĐ)")
    Double minPrice,

    @JsonPropertyDescription("Giá tối đa (VNĐ)")
    Double maxPrice,

    @JsonPropertyDescription("Tên riêng homestay nếu có")
    String homestayName,

    @JsonPropertyDescription("Mảng tiện ích. VD: ['wifi', 'bể bơi']")
    List<String> requestedAmenities,

    @JsonPropertyDescription("Yêu cầu về vibe, cảnh quan, tiện ích khác")
    String semanticQuery
){}
