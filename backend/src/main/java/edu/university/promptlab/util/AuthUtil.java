package edu.university.promptlab.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AuthUtil {

    private final Set<String> adminEmails;

    public AuthUtil(@Value("${app.admin-emails:admin@example.com}") String admins) {
        this.adminEmails = Arrays.stream(admins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    public String extractEmail(JwtAuthenticationToken token) {
        Object claim = token.getToken().getClaims().get("email");
        if (claim == null) {
            throw new IllegalArgumentException("Missing email claim in token");
        }
        return claim.toString();
    }

    public boolean isAdmin(String email) {
        return adminEmails.contains(email.toLowerCase());
    }
}
