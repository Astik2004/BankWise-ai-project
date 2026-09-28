package com.bankwise.knowledgebase.repository;

import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.UUID;

public interface KnowledgeBaseChunkRepository extends MongoRepository<KnowledgeBaseChunk, UUID> {

    List<KnowledgeBaseChunk> findAllByKnowledgeBaseId(UUID knowledgeBaseId);

    List<KnowledgeBaseChunk> findAllByDocumentId(UUID documentId);

    long countByKnowledgeBaseId(UUID knowledgeBaseId);

    void deleteAllByDocumentId(UUID documentId);
}