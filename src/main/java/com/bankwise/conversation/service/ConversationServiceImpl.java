package com.bankwise.conversation.service;

import com.bankwise.common.exception.ResourceNotFoundException;
import com.bankwise.conversation.domain.Conversation;
import com.bankwise.conversation.domain.ConversationStatus;
import com.bankwise.conversation.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private static final String DEFAULT_TITLE = "New Conversation";

    private final ConversationRepository conversationRepository;

    @Override
    public Conversation create(UUID userId, String title) {
        Conversation conversation = Conversation.builder()
                .userId(userId)
                .title(resolveTitle(title))
                .status(ConversationStatus.ACTIVE)
                .messageCount(0)
                .build();

        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation getOwnedConversation(UUID conversationId, UUID userId) {
        return conversationRepository
                .findByIdAndUserId(conversationId, userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conversation not found"
                        )
                );
    }

    @Override
    public List<Conversation> getActiveConversations(UUID userId) {
        return conversationRepository.findAllByUserIdAndStatusOrderByLastMessageAtDesc(userId, ConversationStatus.ACTIVE);
    }

    @Override
    public void updateAfterMessages(Conversation conversation,int count) {
        conversation.updateAfterMessages(count);
        conversationRepository.save(conversation);
    }

    @Override
    public void archive(UUID conversationId, UUID userId) {
        Conversation conversation = getOwnedConversation(conversationId, userId);

        conversation.archive();

        conversationRepository.save(conversation);
    }

    private String resolveTitle(String title) {
        if (!StringUtils.hasText(title)) {
            return DEFAULT_TITLE;
        }

        return title.trim();
    }
}