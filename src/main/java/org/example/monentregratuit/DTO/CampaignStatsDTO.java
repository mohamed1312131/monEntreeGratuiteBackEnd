package org.example.monentregratuit.DTO;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CampaignStatsDTO {
    private Long id;
    private String name;
    private LocalDateTime sentAt;
    private int totalRecipients;
    private int deliveredCount; // Successful sends
    private int failedCount;
    private int openCount;
    private int clickCount;
    private int unsubscribeCount;
    private int spamCount; // Placeholder for now
    private double openRate; // Percentage of delivered emails that were opened
    private double clickRate; // Percentage of delivered emails that were clicked
    private double clickToOpenRate; // Percentage of opened emails that were clicked
}
