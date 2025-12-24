package org.example.monentregratuit.DTO;

import lombok.*;
import org.example.monentregratuit.entity.NewsletterSubscriber;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsletterSubscriberDTO {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private NewsletterSubscriber.SubscriptionStatus status;
    private LocalDateTime subscribedAt;
    private LocalDateTime unsubscribedAt;
    private String unsubscribeReason;
    private Boolean emailBounced;
    private Integer bounceCount;
    private LocalDateTime lastEmailSentAt;
    private Integer totalEmailsSent;
    private String source;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
