package clyvasync.Clyvasync.service.media.listener;

import clyvasync.Clyvasync.enums.media.RingtoneType;
import clyvasync.Clyvasync.event.auth.UserRegisteredEvent;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.service.media.IRingtoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUserRegistrationListener {

    private final IRingtoneService ringtoneService;
    private final UserRepository userRepository;

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        User user = event.user();
        log.info("[MEDIA MODULE] Nghe tin User {} vừa đăng ký. Đang gán nhạc chuông mặc định...", user.getEmail());

        user.setRingtone(ringtoneService.getRingtoneByType(RingtoneType.RINGTONE));

        userRepository.save(user);

        log.info("[MEDIA MODULE] Gán nhạc chuông mặc định thành công!");
    }
}