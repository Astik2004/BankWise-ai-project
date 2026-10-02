package com.bankwise.rag.service;

import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.domain.RagResult;

public interface RagService {

    RagResult answer(RagQuery query);
}