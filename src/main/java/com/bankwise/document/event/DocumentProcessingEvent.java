package com.bankwise.document.event;

import java.util.UUID;

public record DocumentProcessingEvent(UUID documentId) {
}