package com.bankwise.rag.citation;

import java.util.UUID;

public record Citation(
        UUID documentId,
        UUID chunkId,
        int chunkIndex,
        Double score
) {}