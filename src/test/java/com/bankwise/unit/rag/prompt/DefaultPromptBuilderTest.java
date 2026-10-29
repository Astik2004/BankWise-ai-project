package com.bankwise.unit.rag.prompt;

import com.bankwise.rag.domain.RagPrompt;
import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.prompt.DefaultPromptBuilder;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPromptBuilderTest {

    private final DefaultPromptBuilder promptBuilder = new DefaultPromptBuilder();

    @Test
    void testBuildWrapsContextAndQuestionInDelimiters() {
        UUID kbId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        RagQuery query = new RagQuery("What are the minimum balance requirements?", ownerId, kbId);
        String context = "Minimum balance is 5000 INR.";

        RagPrompt prompt = promptBuilder.build(query, context);

        assertNotNull(prompt);
        assertNotNull(prompt.systemInstruction());
        assertTrue(prompt.systemInstruction().contains("BankWise"));
        assertTrue(prompt.systemInstruction().contains("Rules:"));

        String userInstruction = prompt.userInstruction();
        assertTrue(userInstruction.contains("<question>"));
        assertTrue(userInstruction.contains("What are the minimum balance requirements?"));
        assertTrue(userInstruction.contains("</question>"));

        assertTrue(userInstruction.contains("<context>"));
        assertTrue(userInstruction.contains("Minimum balance is 5000 INR."));
        assertTrue(userInstruction.contains("</context>"));
    }
}

