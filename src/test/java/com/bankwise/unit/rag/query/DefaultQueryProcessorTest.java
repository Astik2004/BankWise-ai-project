package com.bankwise.unit.rag.query;

import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.query.DefaultQueryProcessor;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DefaultQueryProcessorTest {

    private final DefaultQueryProcessor processor = new DefaultQueryProcessor();

    @Test
    void testProcessNormalizesWhitespace() {
        UUID ownerId = UUID.randomUUID();
        UUID kbId = UUID.randomUUID();
        RagQuery query = new RagQuery("  What   is   the    interest   rate?  ", ownerId, kbId);

        RagQuery processed = processor.process(query);

        assertEquals("What is the interest rate?", processed.question());
        assertEquals(ownerId, processed.ownerId());
        assertEquals(kbId, processed.knowledgeBaseId());
    }

    @Test
    void testProcessNullQueryThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> processor.process(null));
    }
}

