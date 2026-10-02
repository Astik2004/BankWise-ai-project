package com.bankwise.rag.citation;

import com.bankwise.rag.domain.RetrievedDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class DefaultCitationBuilder implements CitationBuilder {

    private static final String DOCUMENT_ID = "documentId";
    private static final String CHUNK_ID = "chunkId";
    private static final String CHUNK_INDEX = "chunkIndex";

    @Override
    public List<Citation> build(List<RetrievedDocument> documents) {
        List<Citation> citations = documents.stream()
                .map(this::toCitation)
                .filter(citation -> citation != null)
                .toList();

        log.debug(
                "Built {} citations from {} retrieved documents",
                citations.size(),
                documents.size()
        );

        return citations;
    }

    private Citation toCitation(RetrievedDocument document) {
        UUID documentId = toUuid(document.metadata().get(DOCUMENT_ID));

        UUID chunkId = toUuid(document.metadata().get(CHUNK_ID));

        Integer chunkIndex = toInteger(document.metadata().get(CHUNK_INDEX));

        if (documentId == null || chunkId == null || chunkIndex == null) {
            log.warn("Skipping citation for vector document {} due to missing metadata", document.id());
            return null;
        }

        return new Citation(documentId, chunkId, chunkIndex, document.score());
    }

    private UUID toUuid(Object value) {
        if (value == null) {
            return null;
        }

        try {
            return UUID.fromString(value.toString());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}