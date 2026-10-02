package com.bankwise.conversation.repository;

import com.bankwise.conversation.domain.Conversation;
import com.bankwise.conversation.domain.ConversationStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends MongoRepository<Conversation, UUID> {

    Optional<Conversation> findByIdAndUserId(UUID conversationId, UUID userId);

    List<Conversation> findAllByUserIdAndStatusOrderByLastMessageAtDesc(UUID userId, ConversationStatus status);
}