package edu.university.promptlab;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.university.promptlab.api.ChatController;
import edu.university.promptlab.api.dto.ChatRequest;
import edu.university.promptlab.service.GeminiClient;
import edu.university.promptlab.service.RubricService;
import edu.university.promptlab.service.SheetsClient;
import edu.university.promptlab.service.TopicGuard;
import edu.university.promptlab.util.AuthUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ChatControllerTest {

    private TopicGuard topicGuard;
    private RubricService rubricService;
    private SheetsClient sheetsClient;
    private GeminiClient geminiClient;
    private AuthUtil authUtil;
    private ChatController controller;

    @BeforeEach
    void setup() {
        topicGuard = new TopicGuard();
        rubricService = new RubricService();
        sheetsClient = mock(SheetsClient.class);
        geminiClient = mock(GeminiClient.class);
        authUtil = mock(AuthUtil.class);
        controller = new ChatController(topicGuard, rubricService, sheetsClient, geminiClient, authUtil,
                new ObjectMapper(), "sheet-id");
    }

    @Test
    void chat_appendsMessagesAndReturnsResponse() {
        ChatRequest request = new ChatRequest("Explique o teorema de stokes em uma superfície fechada", null, null);
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("email", "student@example.com")
                .build();
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);
        when(authUtil.extractEmail(token)).thenReturn("student@example.com");
        when(geminiClient.generateText(request.prompt()))
                .thenReturn(Mono.just(new GeminiClient.GeminiResponse("Resposta", 123L, 456)));

        var response = controller.chat(request, token).block();

        assertThat(response).isNotNull();
        assertThat(response.blocked()).isFalse();
        assertThat(response.text()).isEqualTo("Resposta");
        verify(sheetsClient, atLeastOnce()).appendRow(eq("messages"), anyList());
        verify(sheetsClient).appendRow(eq("sessions"), anyList());
    }
}
