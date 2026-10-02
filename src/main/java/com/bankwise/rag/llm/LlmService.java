package com.bankwise.rag.llm;

import com.bankwise.rag.domain.RagPrompt;

public interface LlmService {

    String generate(RagPrompt prompt);
}