package com.bankwise.conversation.mapper;

import com.bankwise.conversation.domain.Conversation;
import com.bankwise.conversation.dto.ConversationResponse;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {

    public ConversationResponse toResponse(Conversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getStatus(),
                conversation.getCreatedAt(),
                conversation.getLastMessageAt(),
                conversation.getMessageCount()
        );
    }
}