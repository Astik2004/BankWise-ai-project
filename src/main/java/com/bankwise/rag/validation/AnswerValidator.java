package com.bankwise.rag.validation;

import com.bankwise.rag.domain.RetrievedDocument;

import java.util.List;

public interface AnswerValidator {

    void validate(String answer, List<RetrievedDocument> retrievedDocuments);
}