package com.bankwise.rag.domain;

import java.util.Map;
import java.util.Objects;

public record RetrievedDocument(
        String id,
        String content,
        Map<String, Object> metadata,
        Double score
) {

    public RetrievedDocument {
        Objects.requireNonNull(id, "Document ID must not be null");
        Objects.requireNonNull(content, "Document content must not be null");
        Objects.requireNonNull(metadata, "Document metadata must not be null");

        metadata = Map.copyOf(metadata);
    }
}