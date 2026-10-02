package com.bankwise.rag.domain;

import com.bankwise.rag.citation.Citation;

import java.util.List;
import java.util.Objects;

public record RagResult(
        String answer,
        List<Citation> citations,
        RagSource source
) {

    public RagResult {
        Objects.requireNonNull(answer, "Answer must not be null");
        Objects.requireNonNull(citations, "Citations must not be null");
        Objects.requireNonNull(source, "Source must not be null");

        citations = List.copyOf(citations);
    }

    public static RagResult noAnswer() {
        return new RagResult(
                "I could not find sufficient information in the available banking knowledge base to answer this question.",
                List.of(),
                RagSource.INTERNAL_KNOWLEDGE_BASE
        );
    }
}