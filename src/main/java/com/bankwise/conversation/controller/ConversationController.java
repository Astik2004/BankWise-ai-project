package com.bankwise.conversation.controller;

import com.bankwise.common.response.ApiResponse;
import com.bankwise.conversation.domain.Conversation;
import com.bankwise.conversation.dto.ConversationResponse;
import com.bankwise.conversation.dto.CreateConversationRequest;
import com.bankwise.conversation.mapper.ConversationMapper;
import com.bankwise.conversation.service.ConversationService;
import com.bankwise.security.principal.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final ConversationMapper conversationMapper;

    @PostMapping
    public ResponseEntity<ApiResponse<ConversationResponse>> create(
            @Valid @RequestBody CreateConversationRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Conversation conversation = conversationService.create(principal.getUserId(), request.title());

        ConversationResponse response = conversationMapper.toResponse(conversation);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Conversation created successfully",
                                response
                        )
                );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getAll(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<ConversationResponse> conversations =
                conversationService
                        .getActiveConversations(principal.getUserId())
                        .stream()
                        .map(conversationMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Conversations fetched successfully",
                        conversations
                )
        );
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getById(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Conversation conversation = conversationService.getOwnedConversation(
                        conversationId,
                        principal.getUserId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Conversation fetched successfully",
                        conversationMapper.toResponse(conversation)
                )
        );
    }

    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> archive(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        conversationService.archive(conversationId, principal.getUserId());

        return ResponseEntity.noContent().build();
    }
}