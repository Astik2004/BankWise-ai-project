package com.bankwise.conversation.dto;

import jakarta.validation.constraints.Size;

public record CreateConversationRequest(
        @Size(max = 120, message = "Conversation title must not exceed 120 characters")
        String title
) {
}