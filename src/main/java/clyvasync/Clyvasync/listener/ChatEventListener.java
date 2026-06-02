package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.dto.event.ChatMessageSentEvent;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventListener {

    private final SocketEmitterService socketEmitterService ;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatMessageSent(ChatMessageSentEvent event) {
        socketEmitterService.sendChat(event.conversationId(),event.response());
        if (event.receiverId() != null) {
            socketEmitterService.sendInboxUpdate(event.receiverId(), event.response());
        }
        log.info("Đã bắn WebSocket thành công cho phòng chat {} sau khi DB Commit", event.conversationId());
    }
}