package com.bankwise.rag.domain;

import java.util.Objects;
import java.util.UUID;

public record RagQuery(
        String question,
        UUID ownerId,
        UUID knowledgeBaseId
) {

    private static final int MAX_QUESTION_LENGTH = 2000;

    public RagQuery {
        question = Objects.requireNonNull(question, "Question must not be null").trim();

        ownerId = Objects.requireNonNull(ownerId, "Owner ID must not be null");
        knowledgeBaseId = Objects.requireNonNull(knowledgeBaseId, "Knowledge base ID must not be null");

        if (question.isBlank()) {
            throw new IllegalArgumentException("Question must not be blank");
        }

        if (question.length() > MAX_QUESTION_LENGTH) {
            throw new IllegalArgumentException("Question must not exceed " + MAX_QUESTION_LENGTH + " characters");
        }
    }
}