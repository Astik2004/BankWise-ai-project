package com.bankwise.document.mapper;

import com.bankwise.document.domain.Document;
import com.bankwise.document.dto.DocumentResponse;
import com.bankwise.document.dto.DocumentUploadResponse;

public interface DocumentMapper {

    DocumentResponse toResponse(Document document);

    DocumentUploadResponse toUploadResponse(Document document);
}