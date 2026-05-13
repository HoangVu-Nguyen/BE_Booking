package clyvasync.Clyvasync.modules.homestay.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "homestay_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomestayPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "homestay_id", nullable = false, unique = true)
    private Long homestayId;


    @Builder.Default
    @Column(name = "check_in_time")
    private LocalTime checkInTime = LocalTime.of(14, 0); // Mặc định 14:00:00

    @Builder.Default
    @Column(name = "check_out_time")
    private LocalTime checkOutTime = LocalTime.of(12, 0); // Mặc định 12:00:00

    @Column(name = "late_check_in_instruction", columnDefinition = "TEXT")
    private String lateCheckInInstruction;

    @Builder.Default
    @Column(name = "allows_pets")
    private Boolean allowsPets = false;

    @Builder.Default
    @Column(name = "allows_smoking")
    private Boolean allowsSmoking = false;

    @Builder.Default
    @Column(name = "allows_parties")
    private Boolean allowsParties = false;
}