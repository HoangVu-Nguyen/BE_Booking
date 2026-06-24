package service.util;

import com.pgvector.PGvector;
import java.util.ArrayList;
import java.util.List;

public class VectorUtils {

    /**
     * Chuyển đổi từ List<Double> (Do Spring AI EmbeddingModel trả về)
     * sang PGvector (Để Hibernate hoặc JdbcTemplate lưu vào PostgreSQL)
     */
    public static PGvector toPGVector(float[] floatArray) {
        if (floatArray == null) return null;
        return new PGvector(floatArray);
    }


    public static List<Double> toDoubleList(PGvector pgVector) {
        if (pgVector == null || pgVector.getValue() == null) {
            return new ArrayList<>();
        }

        Object rawValue = pgVector.getValue();
        float[] floatArray;

        if (rawValue instanceof float[]) {
            floatArray = (float[]) rawValue;
        } else if (rawValue instanceof String) {
            String str = ((String) rawValue).replace("[", "").replace("]", "");
            String[] parts = str.split(",");
            floatArray = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                floatArray[i] = Float.parseFloat(parts[i].trim());
            }
        } else {
            throw new IllegalArgumentException("Định dạng vector không xác định: " + rawValue.getClass());
        }

        List<Double> doubleList = new ArrayList<>(floatArray.length);
        for (float v : floatArray) {
            doubleList.add((double) v);
        }
        return doubleList;
    }
}
