package com.bankwise.knowledgebase.embedding;

import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAiEmbeddingService implements EmbeddingService {

    private final VectorStore vectorStore;

    @Override
    public void embed(List<KnowledgeBaseChunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }

        List<Document> documents = chunks.stream()
                        .map(this::toVectorDocument)
                        .toList();

        vectorStore.add(documents);

        log.info("Embedded {} chunks for document {}", chunks.size(), chunks.getFirst().getDocumentId());
    }

    @Override
    public void deleteByChunkIds(List<UUID> chunkIds) {
        if (chunkIds.isEmpty()) {
            return;
        }

        List<String> vectorIds = chunkIds.stream()
                .map(UUID::toString)
                .toList();

        vectorStore.delete(vectorIds);

        log.info("Deleted {} vector embeddings", vectorIds.size());
    }

    private Document toVectorDocument(KnowledgeBaseChunk chunk) {
        Map<String, Object> metadata = chunk.getMetadata() != null
                ? new HashMap<>(chunk.getMetadata())
                : new HashMap<>();

        if (chunk.getKnowledgeBaseId() != null) {
            metadata.put("knowledgeBaseId", chunk.getKnowledgeBaseId().toString());
        }

        if (chunk.getDocumentId() != null) {
            metadata.put("documentId", chunk.getDocumentId().toString());
        }

        if (chunk.getId() != null) {
            metadata.put("chunkId", chunk.getId().toString());
        }

        metadata.put("chunkIndex", chunk.getChunkIndex());

        String docId = chunk.getId() != null ? chunk.getId().toString() : UUID.randomUUID().toString();

        return Document.builder()
                .id(docId)
                .text(chunk.getContent())
                .metadata(metadata)
                .build();
    }
}
