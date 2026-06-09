package clyvasync.Clyvasync.modules.payment.entity;

import clyvasync.Clyvasync.enums.payment.PaymentMethodStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_payment_methods")
@Data
public class UserPaymentMethod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String provider;

    @Column(name = "gateway_token", nullable = false, columnDefinition = "TEXT")
    private String gatewayToken;

    @Column(name = "card_brand", nullable = false, length = 30)
    private String cardBrand;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "last_four", nullable = false, length = 4)
    private String lastFour;

    @Column(name = "exp_month", nullable = false)
    private Integer expMonth;

    @Column(name = "exp_year", nullable = false)
    private Integer expYear;

    @Column(name = "card_holder_name", nullable = false)
    private String cardHolderName;

    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    // ĐÃ SỬA: Chuyển từ String sang Enum mã hóa dạng STRING trong DB
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private PaymentMethodStatus status = PaymentMethodStatus.ACTIVE;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}