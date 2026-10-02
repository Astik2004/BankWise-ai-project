package com.bankwise.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ChatRequest(

        UUID conversationId,

        @NotBlank(message = "Message is required")
        @Size(
                max = 5000,
                message = "Message must not exceed 5000 characters"
        )
        String message
) {
}