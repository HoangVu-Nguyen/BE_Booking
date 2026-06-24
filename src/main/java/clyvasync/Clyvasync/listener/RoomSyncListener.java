package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.dto.event.RoomSearchIndexSyncEvent;
import clyvasync.Clyvasync.service.ai.SearchSyncService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class RoomSyncListener {

    private final SearchSyncService searchSyncService;

    public RoomSyncListener(SearchSyncService searchSyncService) {
        this.searchSyncService = searchSyncService;
    }


    @Async // Chạy ngầm, không làm block luồng chính
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRoomSyncEvent(RoomSearchIndexSyncEvent event) {
        searchSyncService.triggerSyncForRoom(event.getRoomId());
    }
}