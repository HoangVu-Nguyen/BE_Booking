package clyvasync.Clyvasync.service.chat.impl;

import clyvasync.Clyvasync.repository.chat.ConversationParticipantRepository;
import clyvasync.Clyvasync.service.chat.ConversationParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConversationParticipantServiceImpl implements ConversationParticipantService {
    private final ConversationParticipantRepository conversationParticipantRepository;

    @Override
    public boolean existsByConversationIdAndUserId(Long conversationId, Long userId) {
        return conversationParticipantRepository.existsByConversationIdAndUserId(conversationId,userId);
    }

    @Override
    public Optional<Long> findExistingConversationId(Long userId1, Long userId2) {
        return conversationParticipantRepository.findExistingConversationId(userId1,userId2);
    }
}
