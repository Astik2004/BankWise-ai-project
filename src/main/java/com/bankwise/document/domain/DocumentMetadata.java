package com.bankwise.document.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {

    private String originalFileName;

    private String contentType;

    private long fileSize;

    private String extension;

    private String storageKey;
}