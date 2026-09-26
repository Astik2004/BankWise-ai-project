package com.bankwise.document.ingestion.parser;

public interface DocumentParserResolver {

    DocumentParser resolve(String contentType, String extension);
}