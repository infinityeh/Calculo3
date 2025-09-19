package edu.university.promptlab.api.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsentRequest(@NotBlank String version, boolean accepted) {
}
