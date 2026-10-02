package com.bankwise.rag.citation;

import com.bankwise.rag.domain.RetrievedDocument;

import java.util.List;

public interface CitationBuilder {

    List<Citation> build(List<RetrievedDocument> documents);
}