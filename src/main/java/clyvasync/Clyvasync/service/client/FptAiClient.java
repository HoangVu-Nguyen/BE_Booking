package clyvasync.Clyvasync.service.client;

import clyvasync.Clyvasync.dto.response.FptOcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
public class FptAiClient {

    @Value("${fpt.ai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    private static final String FPT_OCR_ENDPOINT = "https://api.fpt.ai/vision/general/vnm/v1";

    public FptOcrResponse scanDocument(String s3PublicUrl) {
        try {
            log.info("Bắt đầu xử lý OCR cho URL: {}", s3PublicUrl);

            byte[] imageBytes = restTemplate.getForObject(s3PublicUrl, byte[].class);

            if (imageBytes == null) {
                throw new RuntimeException("Không thể tải ảnh từ URL: " + s3PublicUrl);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", apiKey);
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            ByteArrayResource fileResource = new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "document.jpg";
                }
            };

            // 4. Đóng gói Body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", fileResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<FptOcrResponse> response = restTemplate.postForEntity(
                    FPT_OCR_ENDPOINT,
                    requestEntity,
                    FptOcrResponse.class
            );

            log.info("FPT OCR trả về mã trạng thái: {}", response.getStatusCode());
            return response.getBody();

        } catch (Exception e) {
            log.error("Lỗi khi gọi API FPT.AI: {}", e.getMessage());
            return new FptOcrResponse(1, "Lỗi kết nối OCR: " + e.getMessage(), null);
        }
    }
}