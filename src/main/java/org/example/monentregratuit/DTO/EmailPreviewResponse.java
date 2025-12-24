package org.example.monentregratuit.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailPreviewResponse {
    private String subject;
    private String htmlContent;
    private String recipientName;
    private String recipientEmail;
}
