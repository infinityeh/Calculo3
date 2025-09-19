package edu.university.promptlab.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "Prompt is required") String prompt,
        String sessionId,
        String assignmentTag
) {
}
