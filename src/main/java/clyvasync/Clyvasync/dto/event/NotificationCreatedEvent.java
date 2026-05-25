package clyvasync.Clyvasync.dto.event;

import clyvasync.Clyvasync.modules.notification.entity.Notification;

public record NotificationCreatedEvent(Notification notification) {}