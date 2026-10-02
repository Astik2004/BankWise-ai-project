package com.bankwise.chat.service;

import com.bankwise.chat.domain.ChatMessage;
import com.bankwise.chat.domain.ChatMessageRole;
import com.bankwise.chat.dto.ChatRequest;
import com.bankwise.chat.dto.ChatResponse;
import com.bankwise.chat.mapper.ChatMapper;
import com.bankwise.chat.repository.ChatMessageRepository;
import com.bankwise.conversation.domain.Conversation;
import com.bankwise.conversation.service.ConversationService;
import com.bankwise.knowledgebase.domain.KnowledgeBase;
import com.bankwise.knowledgebase.service.KnowledgeBaseService;
import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.domain.RagResult;
import com.bankwise.rag.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ConversationService conversationService;
    private final ChatMessageRepository chatMessageRepository;
    private final KnowledgeBaseService knowledgeBaseService;
    private final RagService ragService;
    private final ChatMapper chatMapper;

    @Override
    public ChatResponse chat(ChatRequest request, UUID userId) {
        log.info("Processing chat request for user {}", userId);

        RagResult ragResult = generateAnswer(request.message(), userId);

        Conversation conversation = getOrCreateConversation(request, userId);

        log.debug("Using conversation {} for user {}", conversation.getId(), userId);

        ChatMessage userMessage = saveUserMessage(conversation.getId(), userId, request.message());

        log.debug("User message {} saved for conversation {}", userMessage.getId(), conversation.getId());

        ChatMessage assistantMessage = saveAssistantMessage(conversation.getId(), userId, ragResult.answer());

        conversationService.updateAfterMessages(conversation, 2);

        log.info("Chat request completed for conversation {}", conversation.getId());

        return chatMapper.toResponse(assistantMessage, ragResult);
    }

    private Conversation getOrCreateConversation(ChatRequest request, UUID userId) {

        if (request.conversationId() == null) {
            log.info("Creating new conversation for user {}", userId);
            return conversationService.create(userId, null);
        }

        return conversationService.getOwnedConversation(request.conversationId(), userId);
    }

    private ChatMessage saveUserMessage(UUID conversationId, UUID userId, String message) {

        ChatMessage userMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role(ChatMessageRole.USER)
                .content(message.trim())
                .build();

        return chatMessageRepository.save(userMessage);
    }

    private RagResult generateAnswer(String message, UUID userId) {

        KnowledgeBase knowledgeBase = knowledgeBaseService.getOrCreateDefault(userId);

        log.debug("Using knowledge base {} for user {}", knowledgeBase.getId(), userId);

        RagQuery ragQuery = new RagQuery(message.trim(), userId, knowledgeBase.getId());

        return ragService.answer(ragQuery);
    }

    private ChatMessage saveAssistantMessage(UUID conversationId, UUID userId, String answer) {

        ChatMessage assistantMessage = ChatMessage.builder()
                .conversationId(conversationId)
                .userId(userId)
                .role(ChatMessageRole.ASSISTANT)
                .content(answer)
                .build();

        return chatMessageRepository.save(assistantMessage);
    }
}