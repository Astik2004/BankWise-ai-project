package com.bankwise.document.mapper;

import com.bankwise.document.domain.Document;
import com.bankwise.document.dto.DocumentResponse;
import com.bankwise.document.dto.DocumentUploadResponse;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public DocumentResponse toResponse(Document document) {
        return DocumentResponse.builder()
                .id(document.getId().toString())
                .title(document.getTitle())
                .ownerId(document.getOwnerId())
                .status(document.getStatus())
                .originalFileName(document.getMetadata().getOriginalFileName())
                .contentType(document.getMetadata().getContentType())
                .fileSize(document.getMetadata().getFileSize())
                .extension(document.getMetadata().getExtension())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Override
    public DocumentUploadResponse toUploadResponse(Document document) {
        return DocumentUploadResponse.builder()
                .id(document.getId().toString())
                .title(document.getTitle())
                .status(document.getStatus())
                .build();
    }
}