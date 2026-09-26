package com.bankwise.document.ingestion.metadata;

import java.util.Map;

public interface MetadataExtractor {

    Map<String, Object> extract(String text);
}