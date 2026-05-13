package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "homestay_rooms")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HomestayRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id", nullable = false)
    private Long homestayId; // Chỉ dùng ID, không dùng Entity Homestay

    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String tag;         // 'Master Suite', 'Rare Find'
    private String area;        // '45 m²'
    private String floor;
    private String wing;

    @Column(name = "check_in_time")
    private String checkInTime;

    private Integer maxGuests;
    private Integer bedCount;
    private Integer quantity;
    private String imageUrl;
    private String status;
}