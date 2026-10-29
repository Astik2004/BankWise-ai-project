package com.bankwise.unit.document.ingestion.chunker;

import com.bankwise.document.ingestion.chunker.RecursiveDocumentChunker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecursiveDocumentChunkerTest {

    @Test
    void testChunkSplitsAtSentenceBoundaryWithoutContaminatingNextSentence() {
        // "Hello world. Next sentence." with space
        RecursiveDocumentChunker chunker = new RecursiveDocumentChunker(16, 0);

        String text = "Hello world. Next sentence.";
        List<String> chunks = chunker.chunk(text);

        assertEquals(2, chunks.size());
        assertEquals("Hello world.", chunks.get(0));
        assertEquals("Next sentence.", chunks.get(1));
    }

    @Test
    void testChunkSplitsAtPeriodWithoutSpaceContamination() {
        // "Hello world.Next sentence."
        // Index 11 is '.', Index 12 is 'N'
        // With chunkSize = 16 and overlap = 0:
        // If boundary + 1 is returned (13), chunk 0 becomes "Hello world.N"!
        RecursiveDocumentChunker chunker = new RecursiveDocumentChunker(16, 0);

        String text = "Hello world.Next sentence.";
        List<String> chunks = chunker.chunk(text);

        assertEquals(2, chunks.size());
        assertEquals("Hello world.", chunks.get(0));
        assertEquals("Next sentence.", chunks.get(1));
    }

    @Test
    void testChunkEmptyOrBlankReturnsEmptyList() {
        RecursiveDocumentChunker chunker = new RecursiveDocumentChunker(100, 10);
        assertTrue(chunker.chunk(null).isEmpty());
        assertTrue(chunker.chunk("   ").isEmpty());
    }

    @Test
    void testChunkWithNewlineBoundary() {
        RecursiveDocumentChunker chunker = new RecursiveDocumentChunker(20, 0);
        String text = "Paragraph one.\nParagraph two.";
        List<String> chunks = chunker.chunk(text);

        assertEquals(2, chunks.size());
        assertEquals("Paragraph one.", chunks.get(0));
        assertEquals("Paragraph two.", chunks.get(1));
    }

    @Test
    void testInvalidConfigurationThrows() {
        assertThrows(IllegalArgumentException.class, () -> new RecursiveDocumentChunker(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new RecursiveDocumentChunker(10, 10));
        assertThrows(IllegalArgumentException.class, () -> new RecursiveDocumentChunker(10, 15));
        assertThrows(IllegalArgumentException.class, () -> new RecursiveDocumentChunker(10, -1));
    }
}
