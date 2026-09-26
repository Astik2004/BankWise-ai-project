package com.bankwise.document.ingestion.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface DocumentStorage {

    String store(MultipartFile file, String storageKey);

    InputStream read(String storageKey);

    void delete(String storageKey);

    boolean exists(String storageKey);
}