package com.bankwise.rag.query;

import com.bankwise.rag.domain.RagQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultQueryProcessor implements QueryProcessor {

    @Override
    public RagQuery process(RagQuery query) {
        log.debug("Processing RAG query for knowledge base {}", query.knowledgeBaseId());

        String normalizedQuestion = normalize(query.question());

        return new RagQuery(
                normalizedQuestion,
                query.ownerId(),
                query.knowledgeBaseId()
        );
    }

    private String normalize(String question) {
        return question
                .replaceAll("\\s+", " ")
                .trim();
    }
}