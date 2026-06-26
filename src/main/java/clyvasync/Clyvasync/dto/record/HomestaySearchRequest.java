package clyvasync.Clyvasync.dto.record;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import java.util.List;

public record HomestaySearchRequest(
        @JsonProperty(required = true)
        @JsonPropertyDescription("Ngữ nghĩa chung về không gian hoặc phong cách. BỎ QUA các từ chỉ tiện ích, số giường, giá cả. Ví dụ: 'view đẹp', 'yên tĩnh', 'gần trung tâm'")
        String semanticQuery,

        @JsonPropertyDescription("Tên thành phố hoặc tỉnh thành khách muốn đến. Ví dụ: 'Đà Lạt', 'Vũng Tàu'")
        String city,

        @JsonPropertyDescription("Mức giá tối đa (tính bằng VNĐ) khách có thể trả. Quy đổi: '1 triệu' -> 1000000, '500k' -> 500000")
        Double maxPrice,

        @JsonPropertyDescription("Số lượng giường tối thiểu khách yêu cầu. Ví dụ: khách nói '2 giường' -> điền 2")
        Integer minBeds,

        @JsonPropertyDescription("Số lượng khách tối đa. Ví dụ: 'nhóm 4 người', 'cho 4 người' -> điền 4")
        Integer minGuests,

        @JsonPropertyDescription("Danh sách tên các tiện ích BẮT BUỘC PHẢI CÓ mà khách nhắc đến. Ví dụ: ['bếp', 'hồ bơi', 'máy chiếu']")
        List<String> requiredAmenities
) {}