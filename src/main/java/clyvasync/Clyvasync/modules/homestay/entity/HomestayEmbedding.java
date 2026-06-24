package modules.homestay.entity;

import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "homestay_embedding")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayEmbedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id", nullable = false, unique = true)
    private Long homestayId;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private PGvector embedding;
}