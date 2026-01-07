package org.example.monentregratuit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_blocklist")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailBlocklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "reason")
    private String reason;

    @Column(name = "blocked_at", nullable = false)
    private LocalDateTime blockedAt;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @PrePersist
    protected void onCreate() {
        if (blockedAt == null) {
            blockedAt = LocalDateTime.now();
        }
        if (reason == null) {
            reason = "User unsubscribed";
        }
    }
}
