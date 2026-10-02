package com.bankwise.rag.query;

import com.bankwise.rag.domain.RagQuery;

public interface QueryProcessor {

    RagQuery process(RagQuery query);
}