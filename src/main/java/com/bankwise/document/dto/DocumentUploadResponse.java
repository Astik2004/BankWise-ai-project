package com.bankwise.document.dto;

import com.bankwise.document.domain.DocumentStatus;
import lombok.Builder;

@Builder
public record DocumentUploadResponse(
        String id,
        String title,
        DocumentStatus status
) {
}