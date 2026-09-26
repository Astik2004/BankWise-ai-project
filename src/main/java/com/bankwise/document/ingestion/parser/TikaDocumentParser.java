package com.bankwise.document.ingestion.parser;

import com.bankwise.common.exception.BusinessException;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class TikaDocumentParser {

    public String parse(InputStream inputStream) {
        try {
            TikaDocumentReader reader =
                    new TikaDocumentReader(new InputStreamResource(inputStream));

            List<Document> documents = reader.get();

            return documents.stream()
                    .map(Document::getText)
                    .filter(text -> text != null && !text.isBlank())
                    .reduce("", (first, second) -> first + "\n" + second)
                    .trim();
        } catch (RuntimeException exception) {
            throw new BusinessException("Failed to parse document");
        }
    }
}