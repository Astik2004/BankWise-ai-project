package com.bankwise.unit.rag.citation;

import com.bankwise.rag.citation.Citation;
import com.bankwise.rag.citation.DefaultCitationBuilder;
import com.bankwise.rag.domain.RetrievedDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DefaultCitationBuilderTest {

    private final DefaultCitationBuilder citationBuilder = new DefaultCitationBuilder();

    @Test
    void testBuildWithNullMetadataDoesNotThrowNpe() {
        RetrievedDocument docWithNullMetadata = new RetrievedDocument("id-1", "content", null, 0.9);

        List<Citation> citations = citationBuilder.build(List.of(docWithNullMetadata));

        assertNotNull(citations);
        assertTrue(citations.isEmpty());
    }

    @Test
    void testBuildWithValidMetadataReturnsCitation() {
        UUID docId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();

        Map<String, Object> metadata = Map.of(
                "documentId", docId.toString(),
                "chunkId", chunkId.toString(),
                "chunkIndex", 2
        );

        RetrievedDocument doc = new RetrievedDocument("id-1", "content", metadata, 0.85);

        List<Citation> citations = citationBuilder.build(List.of(doc));

        assertEquals(1, citations.size());
        assertEquals(docId, citations.get(0).documentId());
        assertEquals(chunkId, citations.get(0).chunkId());
        assertEquals(2, citations.get(0).chunkIndex());
        assertEquals(0.85, citations.get(0).score());
    }

    @Test
    void testBuildWithNullListReturnsEmptyList() {
        List<Citation> citations = citationBuilder.build(null);
        assertNotNull(citations);
        assertTrue(citations.isEmpty());
    }
}

