package com.bankwise.document.event;

import com.bankwise.document.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentProcessingEventListener {

    private final DocumentProcessingService documentProcessingService;

    @Async("documentProcessingExecutor")
    @EventListener
    public void handle(DocumentProcessingEvent event) {
        documentProcessingService.process(event.documentId());
    }
}