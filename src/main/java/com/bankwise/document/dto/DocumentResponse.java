package com.bankwise.document.dto;

import com.bankwise.document.domain.DocumentStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record DocumentResponse(
        String id,
        String title,
        UUID ownerId,
        DocumentStatus status,
        String originalFileName,
        String contentType,
        long fileSize,
        String extension,
        Instant createdAt,
        Instant updatedAt
) {
}