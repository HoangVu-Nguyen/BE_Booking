package clyvasync.Clyvasync.service.notification.impl;

import clyvasync.Clyvasync.dto.event.NotificationCreatedEvent;
import clyvasync.Clyvasync.dto.response.NotificationResponse;
import clyvasync.Clyvasync.enums.type.NotificationType;
import clyvasync.Clyvasync.mapper.notification.NotificationMapper;
import clyvasync.Clyvasync.modules.notification.entity.Notification;
import clyvasync.Clyvasync.repository.notification.NotificationRepository;
import clyvasync.Clyvasync.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(Long recipientId, NotificationType type, String title, String message, Map<String, Object> metadata) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .type(type)
                .title(title)
                .message(message)
                .metadata(metadata)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();
      try{
          Notification saved =   notificationRepository.save(notification);
          notificationRepository.flush();
          eventPublisher.publishEvent(new NotificationCreatedEvent(saved));
      }catch (Exception e){
          System.out.println(e.getMessage());
      }

    }

    @Override
    public void sendBatchNotifications(List<Long> recipientIds, NotificationType type, String title, String message, Map<String, Object> metadata) {
        List<Notification> notifications = recipientIds.stream().map(id ->
                Notification.builder()
                        .recipientId(id).type(type).title(title).message(message).metadata(metadata)
                        .isRead(false).createdAt(OffsetDateTime.now()).build()
        ).toList();
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long currentUserId, String filter, Pageable pageable) {
        List<NotificationType> types = mapFilterToTypes(filter);

        return notificationRepository.findAllByRecipientIdAndFilter(currentUserId, types,filter, pageable)
                .map(NotificationMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(Long currentUserId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(currentUserId);
    }

    @Override
    public void markAsRead(Long notificationId, Long currentUserId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            if (n.getRecipientId().equals(currentUserId)) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }

    @Override
    public void markAllAsRead(Long currentUserId) {
        notificationRepository.markAllAsReadByUserId(currentUserId);
    }

    private List<NotificationType> mapFilterToTypes(String filter) {
        // Logic đơn giản để mapping filter string sang list ENUM
        // Nếu filter = BOOKING, return List.of(BOOKING_CREATED, BOOKING_CONFIRMED...)
        return List.of(NotificationType.values()); // Giả sử trả về tất cả nếu chưa lọc chi tiết
    }

}
