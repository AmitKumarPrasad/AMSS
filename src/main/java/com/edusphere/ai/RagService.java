package com.edusphere.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {
    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public RagService(VectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.defaultSystem("Answer only from the supplied school documents. If evidence is insufficient, say so. Include source titles when available.").build();
    }

    public String answer(String question, String schoolId) {
        List<Document> docs = vectorStore.similaritySearch(SearchRequest.builder()
                .query(question)
                .topK(8)
                .filterExpression("schoolId == '" + schoolId.replace("'", "''") + "'")
                .build());
        String context = docs.stream().map(d -> "SOURCE: " + d.getMetadata().getOrDefault("title", "untitled") + "\n" + d.getText())
                .collect(Collectors.joining("\n\n---\n\n"));
        return chatClient.prompt().user("Question: " + question + "\n\nRetrieved evidence:\n" + context).call().content();
    }
}
