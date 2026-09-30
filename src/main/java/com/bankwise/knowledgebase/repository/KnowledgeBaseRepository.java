package com.bankwise.knowledgebase.repository;

import com.bankwise.knowledgebase.domain.KnowledgeBase;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface KnowledgeBaseRepository extends MongoRepository<KnowledgeBase, UUID> {

    Optional<KnowledgeBase> findByOwnerIdAndDefaultKnowledgeBaseTrue(UUID ownerId);

    Optional<KnowledgeBase> findByIdAndOwnerId(UUID knowledgeBaseId, UUID ownerId);

    boolean existsByOwnerIdAndDefaultKnowledgeBaseTrue(UUID ownerId);
}