package com.bankwise.knowledgebase.embedding;

import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;

import java.util.List;
import java.util.UUID;

public interface EmbeddingService {

    void embed(List<KnowledgeBaseChunk> chunks);

    void deleteByChunkIds(List<UUID> chunkIds);
}