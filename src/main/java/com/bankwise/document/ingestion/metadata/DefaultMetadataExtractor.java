package com.bankwise.document.ingestion.metadata;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class DefaultMetadataExtractor implements MetadataExtractor {

    @Override
    public Map<String, Object> extract(String text) {
        if (text == null || text.isBlank()) {
            return Map.of(
                    "characterCount", 0,
                    "wordCount", 0,
                    "lineCount", 0
            );
        }

        Map<String, Object> metadata = new LinkedHashMap<>();

        metadata.put("characterCount", text.length());
        metadata.put("wordCount", countWords(text));
        metadata.put("lineCount", countLines(text));

        return Map.copyOf(metadata);
    }

    private int countWords(String text) {
        return text.trim().split("\\s+").length;
    }

    private int countLines(String text) {
        return text.split("\\R", -1).length;
    }
}