package com.bankwise.rag.llm;

import com.bankwise.common.exception.RagProcessingException;
import com.bankwise.rag.domain.RagPrompt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SpringAiLlmService implements LlmService {

    private final ChatClient chatClient;

    public SpringAiLlmService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String generate(RagPrompt prompt) {
        log.debug("Sending RAG prompt to chat model");

        try {
            String response = chatClient.prompt()
                    .system(prompt.systemInstruction())
                    .user(prompt.userInstruction())
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                throw new RagProcessingException(
                        "LLM returned an empty response"
                );
            }

            log.info(
                    "RAG LLM response generated successfully, length={}",
                    response.length()
            );

            return response.trim();

        } catch (RagProcessingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("RAG LLM invocation failed", exception);
            throw new RagProcessingException(
                    "Failed to generate answer from language model",
                    exception
            );
        }
    }
}