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
                properties.similarityThreshold()
        );

        SearchRequest request = SearchRequest.builder()
                .query(query.question())
                .topK(properties.topK())
                .similarityThreshold(properties.similarityThreshold())
                .filterExpression(
                        "knowledgeBaseId == '" +
                                query.knowledgeBaseId() +
                                "'"
                )
                .build();

        List<Document> documents = vectorStoreRetriever.similaritySearch(request);

        log.info(
                "Retrieved {} documents for knowledge base {}",
                documents.size(),
                query.knowledgeBaseId()
        );

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