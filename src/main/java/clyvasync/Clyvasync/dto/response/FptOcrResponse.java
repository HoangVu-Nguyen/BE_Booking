package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FptOcrResponse {

    // Mã lỗi từ FPT (0 là thành công, khác 0 là lỗi)
    private int errorCode;

    // Thông báo lỗi nếu có
    private String errorMessage;

    // Mảng dữ liệu text bóc tách được
    private List<OcrData> data;

    // Sub-class hứng dữ liệu từng dòng text
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OcrData {
        // Đoạn text mà FPT đọc được
        private String text;

        // Độ tin cậy (VD: 0.99)
        private Double confidence;

        // FPT có trả về cả tọa độ (box) của chữ,
        // nhưng phục vụ việc check Tên thì không cần map vào làm gì cho nặng.
    }

    /**
     * Helper Method: Gom toàn bộ các đoạn text rời rạc thành 1 chuỗi String duy nhất.
     * Cực kỳ hữu dụng để dùng hàm .contains("TÊN_HOST")
     */
    public String getFullText() {
        if (data == null || data.isEmpty()) {
            return "";
        }

        // Nối tất cả các đoạn chữ lại với nhau, cách nhau bởi khoảng trắng
        return data.stream()
                .filter(item -> item.getText() != null)
                .map(OcrData::getText)
                .collect(Collectors.joining(" "));
    }
}