package com.bankwise.knowledgebase.ingestion;

import com.bankwise.document.ingestion.model.IngestedDocument;

public interface KnowledgeBaseIngestion {

    void ingest(IngestedDocument document);
}