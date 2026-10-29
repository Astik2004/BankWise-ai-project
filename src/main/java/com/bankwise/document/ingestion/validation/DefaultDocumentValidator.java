package com.bankwise.document.ingestion.validation;

import com.bankwise.common.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class DefaultDocumentValidator implements DocumentValidator {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "text/plain",
            "text/csv",
            "text/html",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".pdf",
            ".txt",
            ".csv",
            ".html",
            ".htm",
            ".docx",
            ".xlsx",
            ".pptx"
    );

    private final long maxFileSize;

    public DefaultDocumentValidator(@Value("${bankwise.document.max-file-size}") long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    @Override
    public void validate(MultipartFile file) {
        validatePresence(file);
        validateSize(file);
        validateContentType(file);
        validateExtension(file);
    }

    private void validatePresence(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Document file cannot be empty");
        }

        if (file.getOriginalFilename() == null
                || file.getOriginalFilename().isBlank()) {
            throw new BusinessException("Document filename is required");
        }
    }

    private void validateSize(MultipartFile file) {
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("Document size exceeds the allowed limit");
        }
    }

    private void validateContentType(MultipartFile file) {
        String rawContentType = file.getContentType();
        if (rawContentType == null || rawContentType.isBlank()) {
            throw new BusinessException("Document content type is required");
        }

        String mediaType = rawContentType.split(";")[0].trim().toLowerCase();

        if (!ALLOWED_CONTENT_TYPES.contains(mediaType)) {
            throw new BusinessException("Unsupported document type");
        }
    }

    private void validateExtension(MultipartFile file) {
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new BusinessException("Document filename is required");
        }

        int index = filename.lastIndexOf('.');

        if (index < 0 || index == filename.length() - 1) {
            throw new BusinessException("Document extension is required");
        }

        String extension = filename.substring(index).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("Unsupported document extension");
        }
    }
}
