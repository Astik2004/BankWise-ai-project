package com.bankwise.chat.domain;

import com.bankwise.common.model.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage extends BaseEntity {

    private UUID conversationId;

    private UUID userId;

    private ChatMessageRole role;

    private String content;
}