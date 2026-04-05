package clyvasync.Clyvasync.service.media;

import clyvasync.Clyvasync.entity.media.Ringtone;
import clyvasync.Clyvasync.enums.media.RingtoneType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RingtoneService {
    List<Ringtone> getAllRingtones();

    Ringtone createRingtone(Ringtone ringtone);

    Ringtone uploadRingtone(MultipartFile file, RingtoneType type);

    Ringtone getRingtoneByType(RingtoneType type);
}
