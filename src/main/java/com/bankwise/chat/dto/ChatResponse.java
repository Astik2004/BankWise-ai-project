package com.bankwise.chat.dto;

import com.bankwise.rag.citation.Citation;

import java.util.List;
import java.util.UUID;

public record ChatResponse(
        UUID conversationId,
        UUID messageId,
        String answer,
        List<Citation> citations
) {
}