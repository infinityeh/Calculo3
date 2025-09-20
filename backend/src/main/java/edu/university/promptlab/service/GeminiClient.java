package edu.university.promptlab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(WebClient webClient,
                        @Value("${app.gemini.api-key}") String apiKey,
                        @Value("${app.gemini.model:gemini-pro}") String model) {
        this.webClient = webClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    public Mono<GeminiResponse> generateText(String prompt) {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", model, apiKey);
        Map<String, Object> requestBody = Map.of(
                "contents", java.util.List.of(
                        Map.of(
                                "role", "user",
                                "parts", java.util.List.of(Map.of("text", prompt))
                        )
                )
        );
        Instant start = Instant.now();
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorResume(err -> {
                    log.error("Gemini request failed", err);
                    return Mono.just(Map.of());
                })
                .defaultIfEmpty(Map.of())
                .map(response -> {
                    long latency = Duration.between(start, Instant.now()).toMillis();
                    String text = extractText(response);
                    Integer tokens = extractTokens(response);
                    return new GeminiResponse(text, latency, tokens);
                });
    }

    private String extractText(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        try {
            var candidates = (java.util.List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "";
            }
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (java.util.List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                return "";
            }
            Object textObj = parts.get(0).get("text");
            return textObj == null ? "" : textObj.toString();
        } catch (Exception ex) {
            log.warn("Failed to parse Gemini response", ex);
            return "";
        }
    }

    private Integer extractTokens(Map<String, Object> response) {
        if (response == null) {
            return null;
        }
        try {
            Map<String, Object> usageMetadata = (Map<String, Object>) response.get("usageMetadata");
            if (usageMetadata != null && usageMetadata.get("totalTokenCount") != null) {
                return ((Number) usageMetadata.get("totalTokenCount")).intValue();
            }
        } catch (Exception ex) {
            log.debug("Failed to read token usage", ex);
        }
        return null;
    }

    public record GeminiResponse(String text, long latencyMs, Integer tokens) {
    }
}
