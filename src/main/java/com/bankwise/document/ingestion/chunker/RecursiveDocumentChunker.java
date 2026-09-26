package com.bankwise.document.ingestion.chunker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecursiveDocumentChunker implements DocumentChunker {

    private final int chunkSize;
    private final int overlap;

    public RecursiveDocumentChunker(
            @Value("${bankwise.document.chunking.chunk-size}") int chunkSize,
            @Value("${bankwise.document.chunking.overlap}") int overlap
    ) {
        validateConfiguration(chunkSize, overlap);
        this.chunkSize = chunkSize;
        this.overlap = overlap;
    }

    @Override
    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<String> chunks = new ArrayList<>();

        int start = 0;
        int textLength = text.length();

        while (start < textLength) {
            int end = Math.min(start + chunkSize, textLength);

            if (end < textLength) {
                end = findNaturalBoundary(text, start, end);
            }

            String chunk = text.substring(start, end).trim();

            if (!chunk.isBlank()) {
                chunks.add(chunk);
            }

            if (end >= textLength) {
                break;
            }

            start = Math.max(end - overlap, start + 1);
        }

        return List.copyOf(chunks);
    }

    private int findNaturalBoundary(
            String text,
            int start,
            int end
    ) {
        int boundary = findBoundary(text, end, '\n');

        if (boundary > start) {
            return boundary;
        }

        boundary = findBoundary(text, end, '.');

        if (boundary > start) {
            return boundary + 1;
        }

        boundary = findBoundary(text, end, ' ');

        if (boundary > start) {
            return boundary;
        }

        return end;
    }

    private int findBoundary(
            String text,
            int end,
            char delimiter
    ) {
        for (int index = end; index > 0; index--) {
            if (text.charAt(index - 1) == delimiter) {
                return index;
            }
        }

        return -1;
    }

    private void validateConfiguration(
            int chunkSize,
            int overlap
    ) {
        if (chunkSize <= 0) {
            throw new IllegalArgumentException(
                    "Chunk size must be greater than zero"
            );
        }

        if (overlap < 0 || overlap >= chunkSize) {
            throw new IllegalArgumentException(
                    "Chunk overlap must be between zero and chunk size"
            );
        }
    }
}