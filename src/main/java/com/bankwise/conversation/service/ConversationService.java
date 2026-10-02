package com.bankwise.conversation.service;

import com.bankwise.conversation.domain.Conversation;

import java.util.List;
import java.util.UUID;

public interface ConversationService {

    Conversation create(UUID userId, String title);

    Conversation getOwnedConversation(UUID conversationId, UUID userId);

    List<Conversation> getActiveConversations(UUID userId);

    void updateAfterMessages(Conversation conversation,int count);

    void archive(UUID conversationId, UUID userId);
}