package org.example.monentregratuit.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private EmailCampaign campaign;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    // Store ExcelUser data directly to preserve it even after ExcelUser deletion
    @Column(name = "recipient_name")
    private String recipientName;
    
    @Column(name = "recipient_date")
    private String recipientDate;
    
    @Column(name = "recipient_heure")
    private String recipientHeure;
    
    @Column(name = "recipient_code")
    private String recipientCode;
    
    @Column(name = "recipient_foire_name")
    private String recipientFoireName;

    // Optional: Link to ExcelUser if we want to track back to the specific user record
    // This is nullable so EmailLog is preserved even when ExcelUser is deleted
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "excel_user_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
