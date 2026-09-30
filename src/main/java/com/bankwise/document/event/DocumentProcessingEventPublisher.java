package com.bankwise.document.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DocumentProcessingEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(UUID documentId) {
        eventPublisher.publishEvent(new DocumentProcessingEvent(documentId));
    }
}