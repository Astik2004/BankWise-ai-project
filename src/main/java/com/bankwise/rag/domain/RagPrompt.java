package com.bankwise.rag.domain;

public record RagPrompt(
        String systemInstruction,
        String userInstruction
) { }