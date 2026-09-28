package com.bankwise.document.domain;

import com.bankwise.common.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@org.springframework.data.mongodb.core.mapping.Document(collection = "documents")
public class Document extends BaseEntity {

    @Field("owner_id")
    private UUID ownerId;

    @Field("knowledge_base_id")
    private UUID knowledgeBaseId;

    private String title;

    private DocumentMetadata metadata;

    private DocumentStatus status;

    private String failureReason;
}