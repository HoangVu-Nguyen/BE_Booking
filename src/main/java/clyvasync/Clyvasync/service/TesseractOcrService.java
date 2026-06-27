package clyvasync.Clyvasync.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
public class TesseractOcrService {

    public String extractTextFromImage(byte[] imageBytes) {
        try (InputStream is = new ByteArrayInputStream(imageBytes)) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) throw new RuntimeException("Không thể đọc ảnh.");

            // 1. CHỈ ĐƯỜNG CHO JAVA TÌM LÕI C++ (.dylib) CỦA MAC M1/M2
            System.setProperty("jna.library.path", "/opt/homebrew/lib");

            ITesseract tesseract = new Tesseract();

            // 2. CHỈ ĐƯỜNG ĐẾN KHO DATA NGÔN NGỮ MÀ HOMEBREW VỪA CÀI
            // Không cần tạo thư mục tessdata trong project nữa!
            tesseract.setDatapath("/opt/homebrew/share/tessdata");

            // 3. Set ngôn ngữ Tiếng Việt
            tesseract.setLanguage("vie");

            log.info(">>>> [Tesseract] Đang phân tích hình ảnh...");
            String extractedText = tesseract.doOCR(image);
            log.info(">>>> [Tesseract] Quét thành công!");

            return extractedText.toUpperCase();

        } catch (Exception e) {
            log.error(">>>> [Tesseract] Lỗi OCR: {}", e.getMessage());
            return "";
        }
    }
}