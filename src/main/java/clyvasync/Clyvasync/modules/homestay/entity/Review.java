package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter @Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", unique = true, nullable = false)
    private Long bookingId;

    @Column(name = "homestay_id", nullable = false)
    private Long homestayId;

    @Column(name = "guest_id", nullable = false)
    private Long guestId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(name = "reply_from_host", columnDefinition = "TEXT")
    private String replyFromHost;

    private LocalDateTime createdAt;
}