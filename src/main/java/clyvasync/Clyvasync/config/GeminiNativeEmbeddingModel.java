package clyvasync.Clyvasync.config;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Primary
public class GeminiNativeEmbeddingModel implements EmbeddingModel {

    // Tốt nhất nên đổi tên property thành gemini.api-key
    // Nhưng nếu hiện tại ông đang để key trong spring.ai.openai.api-key thì vẫn dùng tạm được
    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String MODEL = "gemini-embedding-001";

    private static final String URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + MODEL
                    + ":embedContent";

    @Override
    public float[] embed(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", "models/" + MODEL,
                "content", Map.of(
                        "parts", List.of(
                                Map.of("text", text)
                        )
                ),
                // Giữ 768 nếu cột PostgreSQL của ông là vector(768)
                "outputDimensionality", 768
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        Map response = restTemplate.postForObject(URL, request, Map.class);

        if (response == null || !response.containsKey("embedding")) {
            throw new RuntimeException("Lỗi phản hồi từ Gemini: " + response);
        }

        Map<String, Object> embeddingNode = (Map<String, Object>) response.get("embedding");
        List<?> values = (List<?>) embeddingNode.get("values");

        if (values == null || values.isEmpty()) {
            throw new RuntimeException("Gemini không trả về embedding values: " + response);
        }

        float[] floatArray = new float[values.size()];

        for (int i = 0; i < values.size(); i++) {
            floatArray[i] = ((Number) values.get(i)).floatValue();
        }

        return floatArray;
    }

    @Override
    public float[] embed(Document document) {
        return embed(document.getText());
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        List<Embedding> embeddings = new ArrayList<>();

        List<String> instructions = request.getInstructions();

        for (int i = 0; i < instructions.size(); i++) {
            float[] vector = embed(instructions.get(i));
            embeddings.add(new Embedding(vector, i));
        }

        return new EmbeddingResponse(embeddings);
    }
}