package com.bankwise.unit.chat.service;

import com.bankwise.chat.domain.ChatMessage;
import com.bankwise.chat.domain.ChatMessageRole;
import com.bankwise.chat.dto.ChatRequest;
import com.bankwise.chat.dto.ChatResponse;
import com.bankwise.chat.mapper.ChatMapper;
import com.bankwise.chat.repository.ChatMessageRepository;
import com.bankwise.chat.service.ChatServiceImpl;
import com.bankwise.common.exception.ResourceNotFoundException;
import com.bankwise.conversation.domain.Conversation;
import com.bankwise.conversation.service.ConversationService;
import com.bankwise.knowledgebase.domain.KnowledgeBase;
import com.bankwise.knowledgebase.service.KnowledgeBaseService;
import com.bankwise.rag.domain.RagQuery;
import com.bankwise.rag.domain.RagResult;
import com.bankwise.rag.domain.RagSource;
import com.bankwise.rag.service.RagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ConversationService conversationService;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private KnowledgeBaseService knowledgeBaseService;

    @Mock
    private RagService ragService;

    @Mock
    private ChatMapper chatMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void testChatWithInvalidConversationIdFailsFastWithoutCallingRag() {
        UUID userId = UUID.randomUUID();
        UUID invalidConversationId = UUID.randomUUID();
        ChatRequest request = new ChatRequest(invalidConversationId, "What are the loan rates?");

        when(conversationService.getOwnedConversation(invalidConversationId, userId))
                .thenThrow(new ResourceNotFoundException("Conversation not found"));

        assertThrows(ResourceNotFoundException.class, () -> chatService.chat(request, userId));

        // RAG Service must NEVER be invoked for an invalid/unauthorized conversation
        verifyNoInteractions(ragService);
        verifyNoInteractions(knowledgeBaseService);
        verifyNoInteractions(chatMessageRepository);
    }

    @Test
    void testChatSuccessfulFlow() {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        UUID kbId = UUID.randomUUID();

        ChatRequest request = new ChatRequest(null, "What is the savings interest rate?");
        Conversation conversation = Conversation.builder().userId(userId).build();
        conversation.setId(conversationId);

        KnowledgeBase kb = KnowledgeBase.builder().ownerId(userId).build();
        kb.setId(kbId);

        RagResult ragResult = new RagResult("Savings interest rate is 4.5%", List.of(), RagSource.INTERNAL_KNOWLEDGE_BASE);

        ChatMessage userMsg = ChatMessage.builder().conversationId(conversationId).userId(userId).role(ChatMessageRole.USER).content(request.message()).build();
        ChatMessage assistantMsg = ChatMessage.builder().conversationId(conversationId).userId(userId).role(ChatMessageRole.ASSISTANT).content(ragResult.answer()).build();
        assistantMsg.setId(UUID.randomUUID());

        ChatResponse expectedResponse = new ChatResponse(conversationId, assistantMsg.getId(), ragResult.answer(), List.of());

        when(conversationService.create(userId, null)).thenReturn(conversation);
        when(knowledgeBaseService.getOrCreateDefault(userId)).thenReturn(kb);
        when(ragService.answer(any(RagQuery.class))).thenReturn(ragResult);
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(userMsg, assistantMsg);
        when(chatMapper.toResponse(assistantMsg, ragResult)).thenReturn(expectedResponse);

        ChatResponse actualResponse = chatService.chat(request, userId);

        assertNotNull(actualResponse);
        assertEquals(conversationId, actualResponse.conversationId());
        assertEquals("Savings interest rate is 4.5%", actualResponse.answer());
        verify(conversationService).updateAfterMessages(conversation, 2);
    }
}

