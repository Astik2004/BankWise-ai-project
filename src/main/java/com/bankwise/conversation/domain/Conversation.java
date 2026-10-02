package com.bankwise.conversation.domain;

import com.bankwise.common.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "conversations")
public class Conversation extends BaseEntity {

    private UUID userId;

    private String title;

    @Builder.Default
    private ConversationStatus status = ConversationStatus.ACTIVE;

    private Instant lastMessageAt;

    @Builder.Default
    private long messageCount = 0;

    public void updateAfterMessages(int count) {
        this.lastMessageAt = Instant.now();
        this.messageCount += count;
    }

    public void archive() {
        this.status = ConversationStatus.ARCHIVED;
    }

    public boolean belongsTo(UUID ownerId) {
        return userId != null && userId.equals(ownerId);
    }
}