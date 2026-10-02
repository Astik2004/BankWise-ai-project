package com.bankwise.conversation.dto;

import com.bankwise.conversation.domain.ConversationStatus;

import java.time.Instant;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        String title,
        ConversationStatus status,
        Instant createdAt,
        Instant lastMessageAt,
        long messageCount
) {
}