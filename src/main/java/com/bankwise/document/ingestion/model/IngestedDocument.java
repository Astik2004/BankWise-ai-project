package com.bankwise.document.ingestion.model;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record IngestedDocument(
        UUID documentId,
        UUID knowledgeBaseId,
        String text,
        Map<String, Object> metadata,
        List<String> chunks
) {}