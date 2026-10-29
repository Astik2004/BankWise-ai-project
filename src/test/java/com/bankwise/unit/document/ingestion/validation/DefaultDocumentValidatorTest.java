package com.bankwise.unit.document.ingestion.validation;

import com.bankwise.common.exception.BusinessException;
import com.bankwise.document.ingestion.validation.DefaultDocumentValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDocumentValidatorTest {

    private DefaultDocumentValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DefaultDocumentValidator(10 * 1024 * 1024); // 10MB
    }

    @Test
    void testValidPdfUploadPasses() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statement.pdf",
                "application/pdf",
                "Dummy PDF content".getBytes()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void testContentTypeWithCharsetParameterPasses() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "statement.txt",
                "text/plain; charset=UTF-8",
                "Sample text content".getBytes()
        );

        assertDoesNotThrow(() -> validator.validate(file));
    }

    @Test
    void testUnsupportedExtensionFails() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "binary content".getBytes()
        );

        assertThrows(BusinessException.class, () -> validator.validate(file));
    }

    @Test
    void testEmptyFileFails() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(BusinessException.class, () -> validator.validate(file));
    }
}

