package clyvasync.Clyvasync.dto.event;

import clyvasync.Clyvasync.dto.detail.WalletNotificationPayload;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class WalletEvent extends ApplicationEvent {
    private final Long ownerId;
    private final WalletNotificationPayload payload;

    public WalletEvent(Object source, Long ownerId, WalletNotificationPayload payload) {
        super(source);
        this.ownerId = ownerId;
        this.payload = payload;
    }
}
