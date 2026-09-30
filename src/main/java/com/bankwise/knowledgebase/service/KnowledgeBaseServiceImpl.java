package com.bankwise.knowledgebase.service;

import com.bankwise.common.exception.ResourceNotFoundException;
import com.bankwise.knowledgebase.domain.KnowledgeBase;
import com.bankwise.knowledgebase.domain.KnowledgeBaseStatus;
import com.bankwise.knowledgebase.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private static final String DEFAULT_NAME = "My Knowledge Base";

    private static final String DEFAULT_DESCRIPTION = "Default knowledge base for uploaded documents";

    private final KnowledgeBaseRepository knowledgeBaseRepository;

    @Override
    public KnowledgeBase getOrCreateDefault(UUID ownerId) {
        return knowledgeBaseRepository
                .findByOwnerIdAndDefaultKnowledgeBaseTrue(ownerId)
                .orElseGet(() -> createDefault(ownerId));
    }

    @Override
    public KnowledgeBase getById(UUID knowledgeBaseId, UUID ownerId) {
        return knowledgeBaseRepository
                .findByIdAndOwnerId(knowledgeBaseId, ownerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Knowledge base not found"
                        )
                );
    }

    private KnowledgeBase createDefault(UUID ownerId) {
        KnowledgeBase knowledgeBase = KnowledgeBase.builder()
                .ownerId(ownerId)
                .name(DEFAULT_NAME)
                .description(DEFAULT_DESCRIPTION)
                .status(KnowledgeBaseStatus.ACTIVE)
                .defaultKnowledgeBase(true)
                .build();

        return knowledgeBaseRepository.save(knowledgeBase);
    }
}