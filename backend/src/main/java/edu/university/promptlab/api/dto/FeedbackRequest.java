package edu.university.promptlab.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FeedbackRequest(
        @NotBlank String messageId,
        @NotNull Boolean thumbsUp,
        @Min(0) @Max(5) int usefulness,
        String comment
) {
}
