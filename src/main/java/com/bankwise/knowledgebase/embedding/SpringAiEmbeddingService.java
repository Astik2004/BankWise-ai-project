package com.bankwise.knowledgebase.embedding;

import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SpringAiEmbeddingService implements EmbeddingService {

    private final VectorStore vectorStore;

    @Override
    public void embed(List<KnowledgeBaseChunk> chunks) {
        if (chunks.isEmpty()) {
            return;
        }

        List<Document> documents =
                chunks.stream()
                        .map(this::toVectorDocument)
                        .toList();

        vectorStore.add(documents);
    }

    private Document toVectorDocument(KnowledgeBaseChunk chunk) {
        Map<String, Object> metadata = new HashMap<>(chunk.getMetadata());

        metadata.put("knowledgeBaseId", chunk.getKnowledgeBaseId().toString());

        metadata.put("documentId", chunk.getDocumentId().toString());

        metadata.put("chunkId", chunk.getId().toString());

        metadata.put("chunkIndex", chunk.getChunkIndex());

        return Document.builder()
                .id(chunk.getId().toString())
                .text(chunk.getContent())
                .metadata(metadata)
                .build();
    }
}