package com.bankwise.knowledgebase.domain;

import com.bankwise.common.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "knowledge_base_chunks")
@CompoundIndex(
        name = "knowledge_base_document_idx",
        def = "{'knowledge_base_id': 1, 'document_id': 1}"
)
public class KnowledgeBaseChunk extends BaseEntity {

    @Field("knowledge_base_id")
    private UUID knowledgeBaseId;

    @Field("document_id")
    private UUID documentId;

    @Field("chunk_index")
    private int chunkIndex;

    private String content;

    private Map<String, Object> metadata;

    private KnowledgeBaseChunkStatus status;
}