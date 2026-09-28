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

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "knowledge_bases")
@CompoundIndex(
        name = "owner_default_idx",
        def = "{'owner_id': 1, 'default_knowledge_base': 1}",
        unique = true
)
public class KnowledgeBase extends BaseEntity {

    @Field("owner_id")
    private UUID ownerId;

    private String name;

    private String description;

    private KnowledgeBaseStatus status;

    @Field("default_knowledge_base")
    private boolean defaultKnowledgeBase;
}