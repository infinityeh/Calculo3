package edu.university.promptlab.service;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class SheetsClient {

    private static final Logger log = LoggerFactory.getLogger(SheetsClient.class);

    private final Sheets sheets;
    private final String spreadsheetId;

    public SheetsClient(Sheets sheets, @org.springframework.beans.factory.annotation.Value("${app.sheets.spreadsheet-id}") String spreadsheetId) {
        this.sheets = sheets;
        this.spreadsheetId = spreadsheetId;
    }

    public void appendRow(String range, List<Object> values) {
        ValueRange body = new ValueRange().setValues(List.of(values));
        try {
            sheets.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("RAW")
                .execute();
        } catch (IOException ex) {
            log.error("Failed to append row to range {}", range, ex);
        }
    }
}
