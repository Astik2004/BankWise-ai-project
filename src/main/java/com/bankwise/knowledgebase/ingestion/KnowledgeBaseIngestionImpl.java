package com.bankwise.knowledgebase.ingestion;

import com.bankwise.document.ingestion.model.IngestedDocument;
import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;
import com.bankwise.knowledgebase.domain.KnowledgeBaseChunkStatus;
import com.bankwise.knowledgebase.embedding.EmbeddingService;
import com.bankwise.knowledgebase.repository.KnowledgeBaseChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseIngestionImpl implements KnowledgeBaseIngestion {

    private final KnowledgeBaseChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    @Override
    public void ingest(IngestedDocument document) {
        chunkRepository.deleteAllByDocumentId(document.documentId());

        List<KnowledgeBaseChunk> chunks = createChunks(document);

        if (chunks.isEmpty()) {
            return;
        }

        List<KnowledgeBaseChunk> savedChunks = chunkRepository.saveAll(chunks);

        try {
            embeddingService.embed(savedChunks);
            markEmbedded(savedChunks);
            chunkRepository.saveAll(savedChunks);

        } catch (RuntimeException exception) {
            markFailed(savedChunks);
            chunkRepository.saveAll(savedChunks);
            throw exception;
        }
    }

    private List<KnowledgeBaseChunk> createChunks(IngestedDocument document) {
        List<String> contentChunks = document.chunks();
        List<KnowledgeBaseChunk> chunks = new ArrayList<>(contentChunks.size());
        for (int index = 0; index < contentChunks.size(); index++) {
            KnowledgeBaseChunk chunk = KnowledgeBaseChunk.builder()
                            .knowledgeBaseId(document.knowledgeBaseId())
                            .documentId(document.documentId())
                            .chunkIndex(index)
                            .content(contentChunks.get(index))
                            .metadata(document.metadata())
                            .status(KnowledgeBaseChunkStatus.CREATED)
                            .build();

            chunks.add(chunk);
        }
        return chunks;
    }

    private void markEmbedded(List<KnowledgeBaseChunk> chunks) {
        chunks.forEach(chunk ->
                chunk.setStatus(KnowledgeBaseChunkStatus.EMBEDDED));
    }

    private void markFailed(List<KnowledgeBaseChunk> chunks) {
        chunks.forEach(chunk ->
                chunk.setStatus(KnowledgeBaseChunkStatus.FAILED));
    }
}