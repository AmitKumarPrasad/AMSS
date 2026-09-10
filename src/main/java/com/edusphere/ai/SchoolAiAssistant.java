package com.edusphere.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class SchoolAiAssistant {
    private final ChatClient chatClient;
    public SchoolAiAssistant(ChatClient.Builder builder) {
        this.chatClient = builder.defaultSystem("You are EduSphere AI, an enterprise school operations assistant. Respect school and role scope. Never invent school facts. Use retrieved evidence for policy questions and clearly distinguish facts from recommendations.").build();
    }
    public String ask(String question) {
        return chatClient.prompt().user(question).call().content();
    }
}
