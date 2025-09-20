package edu.university.promptlab.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;
import reactor.test.publisher.TestPublisher;

import java.time.Duration;

class GeminiClientTest {

    @Test
    void generateTextTimesOutAndReturnsEmptyResponse() {
        TestPublisher<ClientResponse> responsePublisher = TestPublisher.createCold();
        WebClient webClient = WebClient.builder()
                .exchangeFunction(clientRequest -> responsePublisher.mono())
                .build();
        GeminiClient client = new GeminiClient(webClient, "test-key", "test-model");

        StepVerifier.withVirtualTime(() -> client.generateText("prompt"))
                .thenAwait(Duration.ofSeconds(30))
                .expectNextMatches(response -> response.text().isEmpty() && response.tokens() == null)
                .verifyComplete();

        responsePublisher.assertWasCancelled();
    }
}
