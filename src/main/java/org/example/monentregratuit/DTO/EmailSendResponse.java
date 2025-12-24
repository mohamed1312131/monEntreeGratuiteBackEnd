package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailSendResponse {
    private boolean success;
    private String message;
    private int totalEmails;
    private int successfulEmails;
    private int failedEmails;
    private List<String> errors;
}
