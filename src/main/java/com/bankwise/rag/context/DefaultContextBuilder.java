package com.bankwise.rag.context;

import com.bankwise.rag.config.RagRetrievalProperties;
import com.bankwise.rag.domain.RetrievedDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DefaultContextBuilder implements ContextBuilder {

    private final RagRetrievalProperties properties;

    public DefaultContextBuilder(RagRetrievalProperties properties) {
        this.properties = properties;
    }

    @Override
    public String build(List<RetrievedDocument> documents) {
        if (documents.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();

        for (int index = 0; index < documents.size(); index++) {
            RetrievedDocument document = documents.get(index);

            appendDocument(context, document, index + 1);

            if (context.length() >= properties.maxContextCharacters()) {
                break;
            }
        }

        String result = context.toString();

        if (result.length() > properties.maxContextCharacters()) {
            result = result.substring(0, properties.maxContextCharacters());
        }

        log.debug(
                "Built RAG context with {} characters from {} documents",
                result.length(),
                documents.size()
        );

        return result;
    }

    private void appendDocument(StringBuilder context, RetrievedDocument document, int sourceNumber) {
        context.append("\n--- SOURCE ")
                .append(sourceNumber)
                .append(" ---\n");

        context.append("Document ID: ")
                .append(document.id())
                .append('\n');

        context.append("Content:\n")
                .append(document.content())
                .append("\n");
    }
}