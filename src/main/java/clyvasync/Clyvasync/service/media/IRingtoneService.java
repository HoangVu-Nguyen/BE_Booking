package clyvasync.Clyvasync.service.media;

import clyvasync.Clyvasync.enums.media.RingtoneType;
import clyvasync.Clyvasync.modules.media.entity.Ringtone;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IRingtoneService {
    List<Ringtone> getAllRingtones();

    Ringtone createRingtone(Ringtone ringtone);

    Ringtone uploadRingtone(MultipartFile file, RingtoneType type);

    clyvasync.Clyvasync.modules.media.entity.Ringtone getRingtoneByType(RingtoneType type);
}
