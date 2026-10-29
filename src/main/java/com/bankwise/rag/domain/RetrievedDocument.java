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
        content = content != null ? content : "";
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
}
