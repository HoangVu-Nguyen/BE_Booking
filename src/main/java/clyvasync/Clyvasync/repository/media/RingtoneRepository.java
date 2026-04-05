package clyvasync.Clyvasync.repository.media;

import clyvasync.Clyvasync.entity.media.Ringtone;
import clyvasync.Clyvasync.enums.media.RingtoneType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RingtoneRepository extends JpaRepository<Ringtone, Long> {
    Ringtone findRingtoneByType(RingtoneType type);

}
