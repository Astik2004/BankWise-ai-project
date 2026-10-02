package com.bankwise.rag.service;

import com.bankwise.knowledgebase.service.KnowledgeBaseService;
import com.bankwise.rag.citation.Citation;
import com.bankwise.rag.citation.CitationBuilder;
import com.bankwise.rag.context.ContextBuilder;
import com.bankwise.rag.domain.RagPrompt;
import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.domain.RagResult;
import com.bankwise.rag.domain.RagSource;
import com.bankwise.rag.domain.RetrievedDocument;
import com.bankwise.rag.llm.LlmService;
import com.bankwise.rag.prompt.PromptBuilder;
import com.bankwise.rag.query.QueryProcessor;
import com.bankwise.rag.retrieval.DocumentRetriever;
import com.bankwise.rag.validation.AnswerValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagServiceImpl implements RagService {

    private final KnowledgeBaseService knowledgeBaseService;
    private final QueryProcessor queryProcessor;
    private final DocumentRetriever documentRetriever;
    private final ContextBuilder contextBuilder;
    private final PromptBuilder promptBuilder;
    private final LlmService llmService;
    private final AnswerValidator answerValidator;
    private final CitationBuilder citationBuilder;

    @Override
    public RagResult answer(RagQuery query) {
        log.info("Starting RAG request for knowledge base {}", query.knowledgeBaseId());

        validateKnowledgeBaseAccess(query);

        RagQuery processedQuery = queryProcessor.process(query);

        List<RetrievedDocument> documents = documentRetriever.retrieve(processedQuery);

        if (documents.isEmpty()) {
            log.info("No relevant documents found for knowledge base {}", query.knowledgeBaseId());
            return RagResult.noAnswer();
        }

        String context = contextBuilder.build(documents);

        RagPrompt prompt = promptBuilder.build(processedQuery, context);

        String answer = llmService.generate(prompt);

        answerValidator.validate(answer, documents);

        List<Citation> citations = citationBuilder.build(documents);

        log.info(
                "RAG request completed for knowledge base {} with {} citations",
                query.knowledgeBaseId(),
                citations.size()
        );

        return new RagResult(answer, citations, RagSource.INTERNAL_KNOWLEDGE_BASE);
    }

    private void validateKnowledgeBaseAccess(RagQuery query) {
        knowledgeBaseService.getById(query.knowledgeBaseId(), query.ownerId());
    }
}