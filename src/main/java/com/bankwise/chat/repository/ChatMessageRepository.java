package com.bankwise.chat.repository;

import com.bankwise.chat.domain.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, UUID> {

    List<ChatMessage> findAllByConversationIdAndUserIdOrderByCreatedAtAsc(UUID conversationId, UUID userId);
}