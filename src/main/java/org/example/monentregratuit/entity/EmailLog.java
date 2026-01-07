package org.example.monentregratuit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campaign_id", nullable = false)
    private EmailCampaign campaign;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    // Optional: Link to ExcelUser if we want to track back to the specific user record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excel_user_id")
    private ExcelUser excelUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmailStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "tracking_token", unique = true, nullable = false)
    private String trackingToken;

    @Column(name = "opened")
    private boolean opened = false;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "clicked")
    private boolean clicked = false;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "click_count")
    private int clickCount = 0;

    public enum EmailStatus {
        SENT,
        FAILED,
        BOUNCED
    }
}
