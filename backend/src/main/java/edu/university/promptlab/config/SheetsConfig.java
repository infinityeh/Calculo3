package edu.university.promptlab.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class SheetsConfig {

    @Bean
    public Sheets sheetsService() throws GeneralSecurityException, IOException {
        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        var jsonFactory = JacksonFactory.getDefaultInstance();
        GoogleCredential credential = GoogleCredential.getApplicationDefault()
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        return new Sheets.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName("PromptLab")
                .build();
    }
}
