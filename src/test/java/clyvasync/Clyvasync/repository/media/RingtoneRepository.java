package clyvasync.Clyvasync.repository.media;

import clyvasync.Clyvasync.entity.media.Ringtone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RingtoneRepository extends JpaRepository<Long, Ringtone> {
}
