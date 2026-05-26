package clyvasync.Clyvasync.dto.event;

import clyvasync.Clyvasync.dto.response.MessageResponse;

// Class chứa dữ liệu để ném sang Listener
public record ChatMessageSentEvent(
        Long conversationId,
        MessageResponse response
) {}
