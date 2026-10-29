package com.bankwise.unit.document.ingestion.storage;

import com.bankwise.common.exception.BusinessException;
import com.bankwise.document.ingestion.storage.LocalDocumentStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalDocumentStorageTest {

    private LocalDocumentStorage storage;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storage = new LocalDocumentStorage();
        ReflectionTestUtils.setField(storage, "documentPath", tempDir.toString());
    }

    @Test
    void testPathTraversalIsRejected() {
        assertThrows(BusinessException.class, () -> storage.read("../../etc/passwd"));
        assertThrows(BusinessException.class, () -> storage.delete("..\\..\\secret.txt"));
    }

    @Test
    void testEmptyStorageKeyIsRejected() {
        assertThrows(BusinessException.class, () -> storage.read(""));
        assertThrows(BusinessException.class, () -> storage.read(null));
    }
}

