package com.bankwise.document.ingestion;

import com.bankwise.common.exception.ResourceNotFoundException;
import com.bankwise.document.domain.Document;
import com.bankwise.document.ingestion.chunker.DocumentChunker;
import com.bankwise.document.ingestion.metadata.MetadataExtractor;
import com.bankwise.document.ingestion.model.IngestedDocument;
import com.bankwise.document.ingestion.parser.DocumentParser;
import com.bankwise.document.ingestion.parser.DocumentParserResolver;
import com.bankwise.document.ingestion.storage.DocumentStorage;
import com.bankwise.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final DocumentParserResolver parserResolver;
    private final MetadataExtractor metadataExtractor;
    private final DocumentChunker documentChunker;

    @Override
    public IngestedDocument ingest(UUID documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found")
                );

        String contentType =
                document.getMetadata().getContentType();

        String extension =
                document.getMetadata().getExtension();

        DocumentParser parser =
                parserResolver.resolve(
                        contentType,
                        extension
                );

        String storageKey =
                document.getMetadata().getStorageKey();

        String text;

        try (InputStream inputStream =
                     documentStorage.read(storageKey)) {

            text = parser.parse(inputStream);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to ingest document",
                    exception
            );
        }

        Map<String, Object> metadata = metadataExtractor.extract(text);

        List<String> chunks = documentChunker.chunk(text);

        return new IngestedDocument(
                documentId,
                document.getKnowledgeBaseId(),
                text,
                metadata,
                chunks
        );
    }
}