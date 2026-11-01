package com.bankwise.document.controller;

import com.bankwise.common.response.ApiResponse;
import com.bankwise.document.dto.DocumentResponse;
import com.bankwise.document.dto.DocumentUploadResponse;
import com.bankwise.document.service.DocumentService;
import com.bankwise.security.principal.CustomUserPrincipal;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.io.InputStream;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<DocumentUploadResponse>> upload(
            @RequestPart("file") MultipartFile file,
            @RequestPart("title") @NotBlank String title,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        DocumentUploadResponse response = documentService.upload(file, title, principal.getUserId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document uploaded successfully", response));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<ApiResponse<DocumentResponse>> getById(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        DocumentResponse response = documentService.getById(documentId, principal.getUserId());

        return ResponseEntity.ok(ApiResponse.success("Document retrieved successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAll(@AuthenticationPrincipal CustomUserPrincipal principal) {
        List<DocumentResponse> response = documentService.getAll(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success("Documents retrieved successfully", response));
    }


    @GetMapping("/{documentId}/content")
    public ResponseEntity<Resource> getContent(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.getUserId() : null;
        DocumentResponse doc = documentService.getById(documentId, userId);
        InputStream is = documentService.getContent(documentId, userId);
        InputStreamResource resource = new InputStreamResource(is);
        String contentType = doc.contentType() != null ? doc.contentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.originalFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID documentId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        documentService.delete(documentId, principal.getUserId());

        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }
}