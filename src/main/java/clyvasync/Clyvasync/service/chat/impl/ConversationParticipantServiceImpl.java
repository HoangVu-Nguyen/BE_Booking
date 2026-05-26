package clyvasync.Clyvasync.service.chat.impl;

import clyvasync.Clyvasync.repository.chat.ConversationParticipantRepository;
import clyvasync.Clyvasync.service.chat.ConversationParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConversationParticipantServiceImpl implements ConversationParticipantService {
    private final ConversationParticipantRepository conversationParticipantRepository;

    @Override
    public boolean existsByConversationIdAndUserId(Long conversationId, Long userId) {
        return conversationParticipantRepository.existsByConversationIdAndUserId(conversationId,userId);
    }
}
