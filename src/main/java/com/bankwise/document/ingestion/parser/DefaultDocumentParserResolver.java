package com.bankwise.document.ingestion.parser;

import com.bankwise.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DefaultDocumentParserResolver implements DocumentParserResolver {

    private final List<DocumentParser> parsers;

    @Override
    public DocumentParser resolve(String contentType, String extension) {
        return parsers.stream()
                .filter(parser -> parser.supports(contentType, extension))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException("Unsupported document format")
                );
    }
}