package clyvasync.Clyvasync.repository.media;

import clyvasync.Clyvasync.enums.media.RingtoneType;
import clyvasync.Clyvasync.modules.media.entity.Ringtone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RingtoneRepository extends JpaRepository<Ringtone, Long> {
    Ringtone findRingtoneByType(RingtoneType type);

}
