package clyvasync.Clyvasync.modules.tour.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_participant_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TourParticipantDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // PHẲNG HÓA: Chỉ lưu ID của Booking
    @Column(name = "tour_booking_id", nullable = false)
    private Long tourBookingId;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "age")
    private Integer age;

    @Column(name = "identity_number", length = 20)
    private String identityNumber;
}