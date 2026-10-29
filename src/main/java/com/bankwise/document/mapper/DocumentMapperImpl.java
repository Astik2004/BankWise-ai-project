package com.bankwise.document.mapper;

import com.bankwise.document.domain.Document;
import com.bankwise.document.domain.DocumentMetadata;
import com.bankwise.document.dto.DocumentResponse;
import com.bankwise.document.dto.DocumentUploadResponse;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapperImpl implements DocumentMapper {

    @Override
    public DocumentResponse toResponse(Document document) {
        DocumentMetadata metadata = document.getMetadata();

        return DocumentResponse.builder()
                .id(document.getId() != null ? document.getId().toString() : null)
                .title(document.getTitle())
                .ownerId(document.getOwnerId())
                .status(document.getStatus())
                .originalFileName(metadata != null ? metadata.getOriginalFileName() : null)
                .contentType(metadata != null ? metadata.getContentType() : null)
                .fileSize(metadata != null ? metadata.getFileSize() : 0L)
                .extension(metadata != null ? metadata.getExtension() : null)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Override
    public DocumentUploadResponse toUploadResponse(Document document) {
        return DocumentUploadResponse.builder()
                .id(document.getId() != null ? document.getId().toString() : null)
                .title(document.getTitle())
                .status(document.getStatus())
                .build();
    }
}
