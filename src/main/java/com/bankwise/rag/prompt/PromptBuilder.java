package com.bankwise.rag.prompt;

import com.bankwise.rag.domain.RagPrompt;
import com.bankwise.rag.domain.RagQuery;

public interface PromptBuilder {

    RagPrompt build(RagQuery query, String context);
}