package edu.university.promptlab.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.university.promptlab.api.dto.ChatRequest;
import edu.university.promptlab.api.dto.ChatResponse;
import edu.university.promptlab.api.dto.ConsentRequest;
import edu.university.promptlab.api.dto.FeedbackRequest;
import edu.university.promptlab.service.GeminiClient;
import edu.university.promptlab.service.RubricService;
import edu.university.promptlab.service.SheetsClient;
import edu.university.promptlab.service.TopicGuard;
import edu.university.promptlab.util.AuthUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final TopicGuard topicGuard;
    private final RubricService rubricService;
    private final SheetsClient sheetsClient;
    private final GeminiClient geminiClient;
    private final AuthUtil authUtil;
    private final ObjectMapper objectMapper;
    private final String spreadsheetId;

    public ChatController(TopicGuard topicGuard,
                          RubricService rubricService,
                          SheetsClient sheetsClient,
                          GeminiClient geminiClient,
                          AuthUtil authUtil,
                          ObjectMapper objectMapper,
                          @Value("${app.sheets.spreadsheet-id}") String spreadsheetId) {
        this.topicGuard = topicGuard;
        this.rubricService = rubricService;
        this.sheetsClient = sheetsClient;
        this.geminiClient = geminiClient;
        this.authUtil = authUtil;
        this.objectMapper = objectMapper;
        this.spreadsheetId = spreadsheetId;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request, JwtAuthenticationToken token) {
        String email = authUtil.extractEmail(token);
        if (!topicGuard.isAllowed(request.prompt())) {
            log.info("Blocked prompt from {}", email);
            sheetsClient.appendRow("messages",
                    List.of(Instant.now().toString(), email, request.sessionId(), UUID.randomUUID().toString(),
                            "SYSTEM", topicGuard.buildBlockedMessage(), "{}", 0, null, request.assignmentTag()));
            return ChatResponse.blocked(topicGuard.buildBlockedMessage());
        }
        Map<String, Object> rubric = rubricService.buildRubric(request.prompt());
        String rubricJson = serialize(rubric);
        boolean isNewSession = request.sessionId() == null || request.sessionId().isBlank();
        String sessionId = isNewSession ? UUID.randomUUID().toString() : request.sessionId();
        if (isNewSession) {
            sheetsClient.appendRow("sessions",
                    List.of(Instant.now().toString(), email, sessionId, request.assignmentTag()));
        }
        String userMessageId = UUID.randomUUID().toString();
        sheetsClient.appendRow("messages",
                List.of(Instant.now().toString(), email, sessionId, userMessageId, "USER", request.prompt(),
                        rubricJson, null, null, request.assignmentTag()));
        GeminiClient.GeminiResponse response = geminiClient.generateText(request.prompt());
        String assistantMessageId = UUID.randomUUID().toString();
        sheetsClient.appendRow("messages",
                List.of(Instant.now().toString(), email, sessionId, assistantMessageId, "ASSISTANT",
                        response.text(), "{}", response.latencyMs(), response.tokens(), request.assignmentTag()));
        return ChatResponse.success(sessionId, assistantMessageId, response.text());
    }

    @PostMapping("/feedback")
    public ResponseEntity<Void> feedback(@Valid @RequestBody FeedbackRequest request, JwtAuthenticationToken token) {
        String email = authUtil.extractEmail(token);
        sheetsClient.appendRow("feedback",
                List.of(Instant.now().toString(), email, request.messageId(), request.usefulness(),
                        request.thumbsUp(), request.comment()));
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/consent")
    public ResponseEntity<Void> consent(@Valid @RequestBody ConsentRequest request, JwtAuthenticationToken token) {
        String email = authUtil.extractEmail(token);
        sheetsClient.appendRow("consents",
                List.of(Instant.now().toString(), email, request.version(), request.accepted()));
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<String> export(JwtAuthenticationToken token) {
        String email = authUtil.extractEmail(token);
        if (!authUtil.isAdmin(email)) {
            return ResponseEntity.status(403).build();
        }
        String csv = "Download from Google Sheets ID," + spreadsheetId;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=prompt-lab-export.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv);
    }

    private String serialize(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize rubric", e);
            return "{}";
        }
    }
}
