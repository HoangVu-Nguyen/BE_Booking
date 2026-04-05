package clyvasync.Clyvasync.entity.media;

import clyvasync.Clyvasync.enums.media.RingtoneType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ringtones")
@Getter
@Setter
@NoArgsConstructor
public class Ringtone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ringtone_type", nullable = false)
    private RingtoneType type;

    @Column(name = "url", nullable = false, length = 500)
    private String url;
}