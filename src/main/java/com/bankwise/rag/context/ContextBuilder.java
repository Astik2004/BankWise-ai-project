package com.bankwise.rag.context;

import com.bankwise.rag.domain.RetrievedDocument;

import java.util.List;

public interface ContextBuilder {

    String build(List<RetrievedDocument> documents);
}