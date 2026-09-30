package com.bankwise.document.service;

import com.bankwise.document.dto.DocumentResponse;
import com.bankwise.document.dto.DocumentUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface DocumentService {

    DocumentUploadResponse upload(MultipartFile file, String title, UUID ownerId);

    DocumentResponse getById(UUID documentId, UUID ownerId);

    List<DocumentResponse> getAll(UUID ownerId);

    void delete(UUID documentId, UUID ownerId);
}