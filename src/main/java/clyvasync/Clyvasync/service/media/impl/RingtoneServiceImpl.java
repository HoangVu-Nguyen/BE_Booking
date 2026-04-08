package clyvasync.Clyvasync.service.media.impl;

import clyvasync.Clyvasync.enums.media.RingtoneType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.infrastructure.storage.FileStorageService;
import clyvasync.Clyvasync.modules.media.entity.Ringtone;
import clyvasync.Clyvasync.repository.media.RingtoneRepository;
import clyvasync.Clyvasync.service.media.IRingtoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
@Slf4j
@RequiredArgsConstructor
@Service
public class RingtoneServiceImpl implements IRingtoneService {
    private final RingtoneRepository ringtoneRepository;
    private final FileStorageService fileStorageService;

    @Override
    public List<Ringtone> getAllRingtones() {
        return ringtoneRepository.findAll();
    }

    @Override
    public Ringtone createRingtone(Ringtone ringtone) {
        return ringtoneRepository.save(ringtone);
    }

    @Override
    public Ringtone uploadRingtone(MultipartFile file, RingtoneType type) {
        try {
            // Nên có log ở những bước giao tiếp với bên thứ 3 (như AWS S3)
            log.info("Starting to upload ringtone to S3 with type: {}", type);

            String fileUrl = fileStorageService.uploadFile(file);

            Ringtone ringtone = new Ringtone();
            ringtone.setType(type);
            ringtone.setUrl(fileUrl);

            log.info("Upload successful. Saving ringtone to database.");
            return createRingtone(ringtone); // Tái sử dụng hàm create ở trên

        } catch (IOException e) {
            // Chuẩn công ty: Khi bắt Exception, phải log lỗi ra trước khi ném ngoại lệ mới
            log.error("Failed to upload ringtone to S3: {}", e.getMessage(), e);
            throw new AppException(ResultCode.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public clyvasync.Clyvasync.modules.media.entity.Ringtone getRingtoneByType(RingtoneType type) {
        return ringtoneRepository.findRingtoneByType(type);
    }
}
