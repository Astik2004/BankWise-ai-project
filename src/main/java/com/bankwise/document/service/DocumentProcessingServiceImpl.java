package com.bankwise.document.service;

import com.bankwise.document.domain.Document;
import com.bankwise.document.domain.DocumentStatus;
import com.bankwise.document.ingestion.DocumentIngestionService;
import com.bankwise.document.ingestion.model.IngestedDocument;
import com.bankwise.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentProcessingServiceImpl
        implements DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentIngestionService documentIngestionService;

    @Override
    public void process(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new IllegalStateException("Document not found")
                );

        markProcessing(document);

        try {
            IngestedDocument ingestedDocument =
                    documentIngestionService.ingest(documentId);

            handleSuccessfulProcessing(
                    document,
                    ingestedDocument
            );
        } catch (RuntimeException exception) {
            handleProcessingFailure(document, exception);
            throw exception;
        }
    }

    private void markProcessing(Document document) {
        document.setStatus(DocumentStatus.PROCESSING);
        document.setFailureReason(null);
        documentRepository.save(document);
    }

    private void handleSuccessfulProcessing(
            Document document,
            IngestedDocument ingestedDocument
    ) {
        document.setStatus(DocumentStatus.PROCESSED);
        document.setFailureReason(null);
        documentRepository.save(document);
    }

    private void handleProcessingFailure(
            Document document,
            RuntimeException exception
    ) {
        document.setStatus(DocumentStatus.FAILED);
        document.setFailureReason(exception.getMessage());
        documentRepository.save(document);
    }
}