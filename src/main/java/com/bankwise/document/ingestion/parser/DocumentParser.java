package com.bankwise.document.ingestion.parser;

import java.io.InputStream;

public interface DocumentParser {

    boolean supports(String contentType, String extension);

    String parse(InputStream inputStream);
}