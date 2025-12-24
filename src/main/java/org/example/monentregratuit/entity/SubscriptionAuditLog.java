package org.example.monentregratuit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_audit_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscriber_id", nullable = false)
    private Long subscriberId;

    @Column(name = "subscriber_email", nullable = false)
    private String subscriberEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private NewsletterSubscriber.SubscriptionStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private NewsletterSubscriber.SubscriptionStatus newStatus;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "performed_by")
    private String performedBy; // 'user', 'admin', 'system'

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum AuditAction {
        SUBSCRIBED,
        UNSUBSCRIBED,
        RESUBSCRIBED,
        STATUS_CHANGED,
        EMAIL_SENT,
        EMAIL_BOUNCED,
        SPAM_COMPLAINT,
        MANUAL_UPDATE,
        BULK_IMPORT,
        DELETED
    }
}
