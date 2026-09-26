package com.bankwise.document.ingestion.parser;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class TxtDocumentParser implements DocumentParser {

    private final TikaDocumentParser tikaDocumentParser;

    @Override
    public boolean supports(String contentType, String extension) {
        return "text/plain".equalsIgnoreCase(contentType)
                || ".txt".equalsIgnoreCase(extension);
    }

    @Override
    public String parse(InputStream inputStream) {
        return tikaDocumentParser.parse(inputStream);
    }
}