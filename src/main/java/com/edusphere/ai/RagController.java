package com.edusphere.ai;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/rag")
public class RagController {
    private final RagService service;
    public RagController(RagService service) { this.service = service; }

    @PostMapping("/query")
    public Response query(@Valid @RequestBody Request request) {
        return new Response(service.answer(request.question(), request.schoolId()));
    }

    public record Request(@NotBlank String question, @NotBlank String schoolId) {}
    public record Response(String answer) {}
}
