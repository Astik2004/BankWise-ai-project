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
import com.bankwise.knowledgebase.domain.KnowledgeBase;
import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;
import com.bankwise.knowledgebase.embedding.EmbeddingService;
import com.bankwise.knowledgebase.repository.KnowledgeBaseChunkRepository;
import com.bankwise.knowledgebase.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentStorage documentStorage;
    private final DocumentMapper documentMapper;
    private final DocumentValidator documentValidator;
    private final DocumentProcessingEventPublisher processingEventPublisher;
    private final KnowledgeBaseService knowledgeBaseService;
    private final KnowledgeBaseChunkRepository knowledgeBaseChunkRepository;
    private final EmbeddingService embeddingService;

    @Override
    public DocumentUploadResponse upload(MultipartFile file, String title, UUID ownerId) {

        log.info("Uploading document, title={}, ownerId={}", title, ownerId);

        documentValidator.validate(file);

        KnowledgeBase knowledgeBase = knowledgeBaseService.getOrCreateDefault(ownerId);

        UUID documentId = UUID.randomUUID();

        String storageKey = buildStorageKey(documentId, file);

        DocumentMetadata metadata = buildMetadata(file, storageKey);

        documentStorage.store(file, storageKey);

        Document document = Document.builder()
                .ownerId(ownerId)
                .knowledgeBaseId(knowledgeBase.getId())
                .title(title)
                .metadata(metadata)
                .status(DocumentStatus.UPLOADED)
                .build();

        try {
            Document savedDocument = documentRepository.save(document);

            processingEventPublisher.publish(savedDocument.getId());

            log.info("Document uploaded successfully");

            return documentMapper.toUploadResponse(savedDocument);

        } catch (RuntimeException exception) {
            log.warn(exception.getMessage(), exception);
            documentStorage.delete(storageKey);
            throw exception;
        }
    }

    @Override
    public DocumentResponse getById(UUID documentId, UUID ownerId) {
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
    public void delete(UUID documentId, UUID ownerId) {

        Document document = documentRepository.findByIdAndOwnerId(documentId, ownerId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Document not found"
                        )
                );

        log.info("Starting document deletion for document {}", documentId);

        List<KnowledgeBaseChunk> chunks = knowledgeBaseChunkRepository.findAllByDocumentId(documentId);

        deleteEmbeddings(documentId, chunks);

        deleteChunks(documentId);

        deletePhysicalFile(document);

        documentRepository.delete(document);

        log.info("Document {} deleted successfully", documentId);
    }

    private void deleteEmbeddings(UUID documentId, List<KnowledgeBaseChunk> chunks) {
        if (chunks.isEmpty()) {
            log.debug("No knowledge base chunks found for document {}", documentId);
            return;
        }

        List<UUID> chunkIds = chunks.stream()
                .map(KnowledgeBaseChunk::getId)
                .toList();

        embeddingService.deleteByChunkIds(chunkIds);

        log.debug("Deleted embeddings for {} chunks of document {}", chunkIds.size(), documentId);
    }

    private void deleteChunks(UUID documentId) {

        knowledgeBaseChunkRepository.deleteAllByDocumentId(documentId);

        log.debug("Deleted knowledge base chunks for document {}", documentId);
    }

    private void deletePhysicalFile(Document document) {
        String storageKey = document
                .getMetadata()
                .getStorageKey();

        documentStorage.delete(storageKey);

        log.debug("Deleted physical document storage for document {}", document.getId());
    }

    private String buildStorageKey(UUID documentId, MultipartFile file) {

        String extension = extractExtension(file.getOriginalFilename());
        return documentId + extension;
    }

    private DocumentMetadata buildMetadata(MultipartFile file, String storageKey) {

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