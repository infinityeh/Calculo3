package edu.university.promptlab.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

class GeminiClientTest {

    @Test
    void generateTextTimesOutAndReturnsEmptyResponse() {
        WebClient webClient = WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.never())
                .build();
        GeminiClient client = new GeminiClient(webClient, "test-key", "test-model");

        StepVerifier.withVirtualTime(() -> client.generateText("prompt"))
                .thenAwait(Duration.ofSeconds(30))
                .expectNextMatches(response -> response.text().isEmpty() && response.tokens() == null)
                .verifyComplete();
    }
}
