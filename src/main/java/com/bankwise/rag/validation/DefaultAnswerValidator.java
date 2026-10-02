package com.bankwise.rag.validation;

import com.bankwise.common.exception.RagProcessingException;
import com.bankwise.rag.domain.RetrievedDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class DefaultAnswerValidator implements AnswerValidator {

    @Override
    public void validate(String answer, List<RetrievedDocument> retrievedDocuments) {

        if (answer == null || answer.isBlank()) {
            throw new RagProcessingException("Generated answer must not be empty");
        }

        if (retrievedDocuments.isEmpty()) {
            throw new RagProcessingException("Answer cannot be generated without retrieved context");
        }

        log.debug("RAG answer validation passed using {} retrieved documents", retrievedDocuments.size());
    }
}