package com.bankwise.chat.controller;

import com.bankwise.chat.dto.ChatRequest;
import com.bankwise.chat.dto.ChatResponse;
import com.bankwise.chat.service.ChatService;
import com.bankwise.common.response.ApiResponse;
import com.bankwise.security.principal.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(
            @Valid @RequestBody ChatRequest request,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        log.info("Received chat request from user {}", principal.getUserId());

        ChatResponse response = chatService.chat(request, principal.getUserId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Chat response generated successfully",
                        response
                )
        );
    }
}