package com.bankwise.knowledgebase.service;

import com.bankwise.knowledgebase.domain.KnowledgeBase;

import java.util.UUID;

public interface KnowledgeBaseService {

    KnowledgeBase getOrCreateDefault(UUID ownerId);

    KnowledgeBase getById(UUID knowledgeBaseId, UUID ownerId);
}