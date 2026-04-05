package clyvasync.Clyvasync.infrastructure.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
@Service
public interface FileStorageService {
    /**
     * Upload một file lên hệ thống lưu trữ.
     *
     * @param file File được gửi từ client (người dùng).
     * @return Đường dẫn URL (public) để truy cập file sau khi upload thành công.
     * @throws IOException Bắn ra lỗi nếu quá trình đọc/ghi file thất bại.
     */
    String uploadFile(MultipartFile file) throws IOException;

    /**
     * Xóa một file khỏi hệ thống lưu trữ dựa trên URL.
     *
     * @param fileUrl Đường dẫn URL của file cần xóa.
     */
    void deleteFile(String fileUrl);
}
