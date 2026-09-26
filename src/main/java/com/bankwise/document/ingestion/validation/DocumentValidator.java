package com.bankwise.document.ingestion.validation;

import org.springframework.web.multipart.MultipartFile;

public interface DocumentValidator {

    void validate(MultipartFile file);
}