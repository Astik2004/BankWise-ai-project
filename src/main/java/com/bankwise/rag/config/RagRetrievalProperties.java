package com.bankwise.rag.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "bankwise.rag.retrieval")
@Validated
public record RagRetrievalProperties(
        @Min(1)
        @Max(20)
        int topK,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        double similarityThreshold,

        @Min(1000)
        int maxContextCharacters
) {
}