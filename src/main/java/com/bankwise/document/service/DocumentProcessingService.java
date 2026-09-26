package com.bankwise.document.service;

import java.util.UUID;

public interface DocumentProcessingService {

    void process(UUID documentId);
}