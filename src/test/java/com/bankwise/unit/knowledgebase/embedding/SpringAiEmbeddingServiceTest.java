package com.bankwise.unit.knowledgebase.embedding;

import com.bankwise.knowledgebase.domain.KnowledgeBaseChunk;
import com.bankwise.knowledgebase.embedding.SpringAiEmbeddingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringAiEmbeddingServiceTest {

    @Mock
    private VectorStore vectorStore;

    @InjectMocks
    private SpringAiEmbeddingService embeddingService;

    @Test
    void testEmbedChunkWithNullMetadataDoesNotThrowNpe() {
        UUID kbId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();

        KnowledgeBaseChunk chunk = KnowledgeBaseChunk.builder()
                .knowledgeBaseId(kbId)
                .documentId(docId)
                .chunkIndex(0)
                .content("Banking terms and conditions content")
                .metadata(null)
                .build();

        assertDoesNotThrow(() -> embeddingService.embed(List.of(chunk)));

        ArgumentCaptor<List<Document>> captor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(captor.capture());

        List<Document> documents = captor.getValue();
        assertEquals(1, documents.size());
        assertEquals("Banking terms and conditions content", documents.get(0).getText());
        assertEquals(kbId.toString(), documents.get(0).getMetadata().get("knowledgeBaseId"));
        assertEquals(docId.toString(), documents.get(0).getMetadata().get("documentId"));
    }

    @Test
    void testEmbedEmptyListDoesNothing() {
        embeddingService.embed(List.of());
        verifyNoInteractions(vectorStore);
    }

    @Test
    void testDeleteByChunkIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        embeddingService.deleteByChunkIds(List.of(id1, id2));

        verify(vectorStore).delete(List.of(id1.toString(), id2.toString()));
    }
}

