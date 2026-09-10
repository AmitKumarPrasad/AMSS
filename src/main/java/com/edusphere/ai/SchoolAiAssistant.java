package com.edusphere.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

@Service
public class SchoolAiAssistant {
    private final ChatClient chatClient;
    private final ToolCallbackProvider mcpTools;

    public SchoolAiAssistant(ChatClient.Builder builder, ToolCallbackProvider mcpTools) {
        this.chatClient = builder.defaultSystem("You are EduSphere AI, an enterprise school operations assistant. Respect the user's school and role scope. Never invent school facts. Use tools for live operational data and use retrieved documents for policy questions. Clearly separate retrieved facts from recommendations.").build();
        this.mcpTools = mcpTools;
    }

    public String ask(String question) {
        return chatClient.prompt()
                .user(question)
                .tools(mcpTools)
                .call()
                .content();
    }
}
