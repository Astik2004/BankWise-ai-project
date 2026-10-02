package com.bankwise.rag.retrieval;

import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.domain.RetrievedDocument;

import java.util.List;

public interface DocumentRetriever {

    List<RetrievedDocument> retrieve(RagQuery query);
}