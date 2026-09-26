package com.bankwise.document.service;

import com.bankwise.common.exception.ResourceNotFoundException;
import com.bankwise.document.domain.Document;
import com.bankwise.document.domain.DocumentMetadata;
import com.bankwise.document.domain.DocumentStatus;
import com.bankwise.document.dto.DocumentResponse;
import com.bankwise.document.dto.DocumentUploadResponse;
import com.bankwise.document.event.DocumentProcessingEventPublisher;
import com.bankwise.document.ingestion.storage.DocumentStorage;
import com.bankwise.document.ingestion.validation.DocumentValidator;
import com.bankwise.document.mapper.DocumentMapper;
import com.bankwise.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final DocumentMapper documentMapper;
    private final DocumentValidator documentValidator;
    private final DocumentProcessingEventPublisher processingEventPublisher;

    @Override
    public DocumentUploadResponse upload(
            MultipartFile file,
            String title,
            UUID ownerId
    ) {
        documentValidator.validate(file);

        UUID documentId = UUID.randomUUID();
        String storageKey = buildStorageKey(documentId, file);

        DocumentMetadata metadata = buildMetadata(file, storageKey);

        documentStorage.store(file, storageKey);

        Document document = Document.builder()
                .ownerId(ownerId)
                .title(title)
                .metadata(metadata)
                .status(DocumentStatus.UPLOADED)
                .build();

        try {
            Document savedDocument = documentRepository.save(document);

            processingEventPublisher.publish(savedDocument.getId());

            return documentMapper.toUploadResponse(savedDocument);
        } catch (RuntimeException exception) {
            documentStorage.delete(storageKey);
            throw exception;
        }
    }

    @Override
    public DocumentResponse getById(
            UUID documentId,
            UUID ownerId
    ) {
        Document document = documentRepository
                .findByIdAndOwnerId(documentId, ownerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found")
                );

        return documentMapper.toResponse(document);
    }

    @Override
    public List<DocumentResponse> getAll(UUID ownerId) {
        return documentRepository
                .findAllByOwnerId(ownerId)
                .stream()
                .map(documentMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(
            UUID documentId,
            UUID ownerId
    ) {
        Document document = documentRepository
                .findByIdAndOwnerId(documentId, ownerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Document not found")
                );

        String storageKey = document.getMetadata().getStorageKey();

        documentStorage.delete(storageKey);
        documentRepository.delete(document);
    }

    private String buildStorageKey(
            UUID documentId,
            MultipartFile file
    ) {
        String extension = extractExtension(file.getOriginalFilename());

        return documentId + extension;
    }

    private DocumentMetadata buildMetadata(
            MultipartFile file,
            String storageKey
    ) {
        String originalFileName = file.getOriginalFilename();
        String extension = extractExtension(originalFileName);

        return DocumentMetadata.builder()
                .originalFileName(originalFileName)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .extension(extension)
                .storageKey(storageKey)
                .build();
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');

        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(index).toLowerCase();
    }
}