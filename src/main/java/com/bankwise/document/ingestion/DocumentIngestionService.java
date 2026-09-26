package com.bankwise.document.ingestion;

import com.bankwise.document.ingestion.model.IngestedDocument;

import java.util.UUID;

public interface DocumentIngestionService {

    IngestedDocument ingest(UUID documentId);
}