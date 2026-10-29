package com.bankwise.document.ingestion.storage;

import com.bankwise.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class LocalDocumentStorage implements DocumentStorage {

    @Value("${bankwise.storage.document-path}")
    private String documentPath;

    @Override
    public String store(MultipartFile file, String storageKey) {
        Path target = resolvePath(storageKey);

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
            return storageKey;
        } catch (IOException exception) {
            throw new BusinessException("Failed to store document");
        }
    }

    @Override
    public InputStream read(String storageKey) {
        Path path = resolvePath(storageKey);

        try {
            return Files.newInputStream(path);
        } catch (IOException exception) {
            throw new BusinessException("Failed to read document");
        }
    }

    @Override
    public void delete(String storageKey) {
        Path path = resolvePath(storageKey);

        try {
            Files.deleteIfExists(path);
        } catch (IOException exception) {
            throw new BusinessException("Failed to delete document");
        }
    }

    @Override
    public boolean exists(String storageKey) {
        return Files.exists(resolvePath(storageKey));
    }

    private Path resolvePath(String storageKey) {
        if (!StringUtils.hasText(storageKey)) {
            throw new BusinessException("Storage key must not be empty");
        }

        Path basePath = Paths.get(documentPath).toAbsolutePath().normalize();
        Path resolvedPath = basePath.resolve(storageKey).normalize();

        if (!resolvedPath.startsWith(basePath)) {
            throw new BusinessException("Invalid storage key: path traversal detected");
        }

        return resolvedPath;
    }
}
