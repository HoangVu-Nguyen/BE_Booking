package clyvasync.Clyvasync.dto.response;


import lombok.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;          // Danh sách dữ liệu của trang hiện tại
    private long totalElements;      // Tổng số bản ghi trong DB (để hiện: "Showing 10 of 100")
    private int totalPages;          // Tổng số trang (để render các nút số 1, 2, 3...)
    private int size;                // Số lượng phần tử mỗi trang (pageSize)
    private int number;              // Số thứ tự trang hiện tại (pageNumber)
    private boolean last;            // Có phải trang cuối cùng không? (để ẩn nút "Load more")
    private boolean first;           // Có phải trang đầu tiên không?

    // Metadata bổ sung (Rất quan trọng cho các phần như Review)
    private Double averageRating;    // Điểm trung bình (Dùng cho section Review)
    private Long extraData;          // Bất kỳ thông tin phụ nào khác nếu cần
}