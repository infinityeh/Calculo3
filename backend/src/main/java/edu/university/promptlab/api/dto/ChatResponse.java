package edu.university.promptlab.api.dto;

public record ChatResponse(String sessionId, String assistantMessageId, String text, boolean blocked, String system) {
    public static ChatResponse blocked(String reason) {
        return new ChatResponse(null, null, null, true, reason);
    }

    public static ChatResponse success(String sessionId, String assistantMessageId, String text) {
        return new ChatResponse(sessionId, assistantMessageId, text, false, null);
    }
}
