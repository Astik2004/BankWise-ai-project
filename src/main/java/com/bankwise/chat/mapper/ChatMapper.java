package com.bankwise.chat.mapper;

import com.bankwise.chat.domain.ChatMessage;
import com.bankwise.chat.dto.ChatResponse;
import com.bankwise.rag.domain.RagResult;
import org.springframework.stereotype.Component;

@Component
public class ChatMapper {

    public ChatResponse toResponse(
            ChatMessage assistantMessage,
            RagResult ragResult
    ) {
        return new ChatResponse(
                assistantMessage.getConversationId(),
                assistantMessage.getId(),
                ragResult.answer(),
                ragResult.citations()
        );
    }
}