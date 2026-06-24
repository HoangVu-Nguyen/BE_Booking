package clyvasync.Clyvasync.dto.event;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class RoomSearchIndexSyncEvent extends ApplicationEvent {
    private final Long roomId;

    public RoomSearchIndexSyncEvent(Object source, Long roomId) {
        super(source);
        this.roomId = roomId;
    }
}