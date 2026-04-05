package clyvasync.Clyvasync.service.media.listener;

import clyvasync.Clyvasync.entity.auth.User;
import clyvasync.Clyvasync.enums.media.RingtoneType;
import clyvasync.Clyvasync.event.auth.UserRegisteredEvent;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.service.media.RingtoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaUserRegistrationListener {

    private final RingtoneService ringtoneService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        User user = event.user();
        log.info("[MEDIA MODULE] Nghe tin User {} vừa đăng ký. Đang gán nhạc chuông mặc định...", user.getEmail());

        user.setRingtone(ringtoneService.getRingtoneByType(RingtoneType.RINGTONE));

        userRepository.save(user);

        log.info("[MEDIA MODULE] Gán nhạc chuông mặc định thành công!");
    }
}