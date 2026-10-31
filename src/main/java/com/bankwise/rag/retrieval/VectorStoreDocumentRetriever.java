package com.bankwise.rag.retrieval;

import com.bankwise.rag.config.RagRetrievalProperties;
import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.domain.RetrievedDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStoreRetriever;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class VectorStoreDocumentRetriever implements DocumentRetriever {

    private final VectorStoreRetriever vectorStoreRetriever;
    private final RagRetrievalProperties properties;

    public VectorStoreDocumentRetriever(VectorStoreRetriever vectorStoreRetriever, RagRetrievalProperties properties) {
        this.vectorStoreRetriever = vectorStoreRetriever;
        this.properties = properties;
    }

    @Override
    public List<RetrievedDocument> retrieve(RagQuery query) {
        log.debug(
                "Retrieving documents for knowledge base {} with topK={} and threshold={}",
                query.knowledgeBaseId(),
                properties.topK(),
                properties.similarityThreshold());

        List<Document> documents = List.of();

        // 1. Primary search: filter by knowledgeBaseId
        try {
            SearchRequest request = SearchRequest.builder()
                    .query(query.question())
                    .topK(properties.topK())
                    .similarityThreshold(properties.similarityThreshold())
                    .filterExpression(
                            "knowledgeBaseId == '" +
                                    query.knowledgeBaseId() +
                                    "'")
                    .build();

            documents = vectorStoreRetriever.similaritySearch(request);
            log.debug("KnowledgeBase filtered search returned {} documents", documents.size());
        } catch (Exception exception) {
            log.warn("Knowledge base filtered search encountered error: {}. Falling back to broad search.",
                    exception.getMessage());
        }

        // 2. Fallback search: if no documents matched the filter or if filter failed,
        // search across the bank's vector store
        if (documents == null || documents.isEmpty()) {
            log.info(
                    "No documents found with knowledgeBaseId filter. Executing broad similarity search across bank knowledge base.");
            try {
                SearchRequest broadRequest = SearchRequest.builder()
                        .query(query.question())
                        .topK(properties.topK())
                        .similarityThreshold(Math.min(properties.similarityThreshold(), 0.50))
                        .build();

                documents = vectorStoreRetriever.similaritySearch(broadRequest);
                log.info("Broad similarity search retrieved {} documents", documents.size());
            } catch (Exception exception) {
                log.error("Broad similarity search encountered error: {}", exception.getMessage(), exception);
                documents = List.of();
            }
        }

        log.info(
                "Final retrieved {} documents for query: '{}'",
                documents.size(),
                query.question());

        return documents.stream()
                .map(this::toRetrievedDocument)
                .toList();
    }

    private RetrievedDocument toRetrievedDocument(Document document) {
        return new RetrievedDocument(
                document.getId(),
                document.getText(),
                document.getMetadata(),
                document.getScore()
        );
    }
}
