package com.bankwise.knowledgebase.embedding;

import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;

import java.util.List;

public interface EmbeddingService {

    void embed(List<KnowledgeBaseChunk> chunks);
}