package com.edusphere.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AiAssistantController {
    private final SchoolAiAssistant assistant;
    public AiAssistantController(SchoolAiAssistant assistant) { this.assistant = assistant; }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(assistant.ask(request.message()));
    }

    public record ChatRequest(@NotBlank String message) {}
    public record ChatResponse(String answer) {}
}
