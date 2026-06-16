package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class HomestayRequest {
    private String name;

    // ĐÃ THÊM: Hứng ID của loại hình (Căn hộ = 1, Biệt thự = 2...)
    private Integer categoryId;

    // ĐÃ SỬA: Đổi từ 'address' thành 'addressDetail' cho khớp DB
    private String addressDetail;

    private BigDecimal latitude;
    private BigDecimal longitude;

    // ĐÃ SỬA: Đổi từ 'imageUrls' thành 'objectKeys' cho chuẩn luồng S3
    private List<String> objectKeys;

    // Các trường dưới đây giữ nguyên, dùng cho bước Đắp nội thất sau này
    private String description;
    private String city;
    private BigDecimal basePrice;
    private Integer maxGuests;
    private Integer numBedrooms;
    private Integer numBathrooms;
    private Set<Long> amenityIds;
    private List<ImageSubmitRequest> images;
}