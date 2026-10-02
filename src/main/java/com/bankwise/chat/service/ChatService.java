package com.bankwise.chat.service;

import com.bankwise.chat.dto.ChatRequest;
import com.bankwise.chat.dto.ChatResponse;

import java.util.UUID;

public interface ChatService {
    ChatResponse chat(ChatRequest request, UUID userId);
}