package com.bankwise.document.repository;

import com.bankwise.document.domain.Document;
import com.bankwise.document.domain.DocumentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends MongoRepository<Document, UUID> {

    List<Document> findAllByOwnerId(UUID ownerId);

    Optional<Document> findByIdAndOwnerId(UUID id, UUID ownerId);

    boolean existsByIdAndOwnerId(UUID id, UUID ownerId);

    List<Document> findAllByOwnerIdAndStatus(
            UUID ownerId,
            DocumentStatus status
    );
}