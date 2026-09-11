package com.edusphere.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolAiAssistant {
    private final ChatClient chatClient;

    public SchoolAiAssistant(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem("You are EduSphere AI, an enterprise school operations assistant. Respect school and role scope. Never invent school facts. Use retrieved evidence for policy questions and clearly distinguish facts from recommendations.").build();
    }

    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }

    public String answer(String question, List<RagService.RagResult> evidence) {
        String context = evidence.stream()
                .map(item -> "SOURCE: " + item.title() + " (" + item.source() + ")\n" + item.content())
                .reduce((left, right) -> left + "\n\n---\n\n" + right)
                .orElse("No relevant school documents were retrieved.");
        return chatClient.prompt()
                .user("Question: " + question + "\n\nRetrieved school evidence:\n" + context
                        + "\n\nAnswer using the evidence. If it does not contain enough information, explicitly say that the evidence is insufficient.")
                .call()
                .content();
    }
}
