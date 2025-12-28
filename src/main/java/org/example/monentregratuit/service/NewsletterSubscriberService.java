package org.example.monentregratuit.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.example.monentregratuit.DTO.NewsletterSubscriberDTO;
import org.example.monentregratuit.entity.NewsletterSubscriber;
import org.example.monentregratuit.entity.SubscriptionAuditLog;
import org.example.monentregratuit.repo.NewsletterSubscriberRepository;
import org.example.monentregratuit.repo.SubscriptionAuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NewsletterSubscriberService {

    private final NewsletterSubscriberRepository subscriberRepository;
    private final SubscriptionAuditLogRepository auditLogRepository;

    @Transactional
    public NewsletterSubscriberDTO subscribe(String email, String name, String phone, String source, HttpServletRequest request) {
        // Check if already exists
        if (subscriberRepository.existsByEmail(email)) {
            NewsletterSubscriber existing = subscriberRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Subscriber not found"));
            
            // If previously unsubscribed, reactivate
            if (existing.getStatus() != NewsletterSubscriber.SubscriptionStatus.ACTIVE) {
                existing.setStatus(NewsletterSubscriber.SubscriptionStatus.ACTIVE);
                existing.setSubscribedAt(LocalDateTime.now());
                existing.setUnsubscribedAt(null);
                existing.setUnsubscribeReason(null);
                
                subscriberRepository.save(existing);
                
                logAudit(existing.getId(), email, SubscriptionAuditLog.AuditAction.RESUBSCRIBED,
                        existing.getStatus(), NewsletterSubscriber.SubscriptionStatus.ACTIVE,
                        "User resubscribed", getClientIP(request), getUserAgent(request), "user", null);
            }
            
            return convertToDTO(existing);
        }

        // Create new subscriber
        NewsletterSubscriber subscriber = NewsletterSubscriber.builder()
                .email(email)
                .name(name)
                .phone(phone)
                .status(NewsletterSubscriber.SubscriptionStatus.ACTIVE)
                .subscribedAt(LocalDateTime.now())
                .unsubscribeToken(UUID.randomUUID().toString())
                .emailBounced(false)
                .bounceCount(0)
                .totalEmailsSent(0)
                .ipAddress(getClientIP(request))
                .source(source != null ? source : "website")
                .build();

        subscriber = subscriberRepository.save(subscriber);

        logAudit(subscriber.getId(), email, SubscriptionAuditLog.AuditAction.SUBSCRIBED,
                null, NewsletterSubscriber.SubscriptionStatus.ACTIVE,
                "New subscription", getClientIP(request), getUserAgent(request), "user", null);

        return convertToDTO(subscriber);
    }

    @Transactional
    public void unsubscribe(String token, String reason, HttpServletRequest request) {
        NewsletterSubscriber subscriber = subscriberRepository.findByUnsubscribeToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid unsubscribe token"));

        if (subscriber.getStatus() == NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED) {
            return; // Already unsubscribed
        }

        NewsletterSubscriber.SubscriptionStatus oldStatus = subscriber.getStatus();
        subscriber.setStatus(NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriber.setUnsubscribeReason(reason);

        subscriberRepository.save(subscriber);

        logAudit(subscriber.getId(), subscriber.getEmail(), SubscriptionAuditLog.AuditAction.UNSUBSCRIBED,
                oldStatus, NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED,
                reason, getClientIP(request), getUserAgent(request), "user", null);
    }

    @Transactional
    public void unsubscribeByEmail(String email, String reason, HttpServletRequest request) {
        NewsletterSubscriber subscriber = subscriberRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        if (subscriber.getStatus() == NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED) {
            return;
        }

        NewsletterSubscriber.SubscriptionStatus oldStatus = subscriber.getStatus();
        subscriber.setStatus(NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED);
        subscriber.setUnsubscribedAt(LocalDateTime.now());
        subscriber.setUnsubscribeReason(reason);

        subscriberRepository.save(subscriber);

        logAudit(subscriber.getId(), email, SubscriptionAuditLog.AuditAction.UNSUBSCRIBED,
                oldStatus, NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED,
                reason, getClientIP(request), getUserAgent(request), "user", null);
    }

    @Transactional
    public NewsletterSubscriberDTO updateSubscriber(Long id, NewsletterSubscriberDTO dto, Long adminId) {
        NewsletterSubscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        NewsletterSubscriber.SubscriptionStatus oldStatus = subscriber.getStatus();

        subscriber.setName(dto.getName());
        subscriber.setPhone(dto.getPhone());
        
        if (dto.getStatus() != null && dto.getStatus() != oldStatus) {
            subscriber.setStatus(dto.getStatus());
            
            if (dto.getStatus() == NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED) {
                subscriber.setUnsubscribedAt(LocalDateTime.now());
            }
        }

        subscriber = subscriberRepository.save(subscriber);

        if (oldStatus != subscriber.getStatus()) {
            logAudit(subscriber.getId(), subscriber.getEmail(), SubscriptionAuditLog.AuditAction.MANUAL_UPDATE,
                    oldStatus, subscriber.getStatus(),
                    "Status updated by admin", null, null, "admin", adminId);
        }

        return convertToDTO(subscriber);
    }

    @Transactional
    public void deleteSubscriber(Long id, Long adminId) {
        NewsletterSubscriber subscriber = subscriberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        logAudit(subscriber.getId(), subscriber.getEmail(), SubscriptionAuditLog.AuditAction.DELETED,
                subscriber.getStatus(), null,
                "Subscriber deleted by admin", null, null, "admin", adminId);

        subscriberRepository.delete(subscriber);
    }

    @Transactional
    public void recordEmailSent(Long subscriberId) {
        NewsletterSubscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));

        subscriber.setLastEmailSentAt(LocalDateTime.now());
        subscriber.setTotalEmailsSent(subscriber.getTotalEmailsSent() + 1);
        subscriberRepository.save(subscriber);

        logAudit(subscriber.getId(), subscriber.getEmail(), SubscriptionAuditLog.AuditAction.EMAIL_SENT,
                null, null, "Newsletter email sent", null, null, "system", null);
    }

    @Transactional
    public void recordEmailBounce(String email) {
        subscriberRepository.findByEmail(email).ifPresent(subscriber -> {
            subscriber.setBounceCount(subscriber.getBounceCount() + 1);
            
            if (subscriber.getBounceCount() >= 3) {
                subscriber.setEmailBounced(true);
                subscriber.setStatus(NewsletterSubscriber.SubscriptionStatus.BOUNCED);
            }
            
            subscriberRepository.save(subscriber);

            logAudit(subscriber.getId(), email, SubscriptionAuditLog.AuditAction.EMAIL_BOUNCED,
                    null, null, "Email bounced (count: " + subscriber.getBounceCount() + ")",
                    null, null, "system", null);
        });
    }

    public Page<NewsletterSubscriberDTO> getAllSubscribers(Pageable pageable) {
        return subscriberRepository.findAll(pageable).map(this::convertToDTO);
    }

    public Page<NewsletterSubscriberDTO> getSubscribersByStatus(NewsletterSubscriber.SubscriptionStatus status, Pageable pageable) {
        return subscriberRepository.findByStatus(status, pageable).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Page<NewsletterSubscriberDTO> searchSubscribers(
            NewsletterSubscriber.SubscriptionStatus status,
            String search,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            Pageable pageable) {
        
        Specification<NewsletterSubscriber> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (search != null && !search.trim().isEmpty()) {
                String searchTerm = "%" + search.trim().toLowerCase() + "%";
                Predicate emailPredicate = cb.like(cb.lower(root.get("email")), searchTerm);
                Predicate namePredicate = cb.like(cb.lower(root.get("name")), searchTerm);
                predicates.add(cb.or(emailPredicate, namePredicate));
            }

            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("subscribedAt"), dateFrom));
            }

            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("subscribedAt"), dateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        
        return subscriberRepository.findAll(spec, pageable)
                .map(this::convertToDTO);
    }

    public List<NewsletterSubscriberDTO> getActiveSubscribers() {
        return subscriberRepository.findAllActiveSubscribers(NewsletterSubscriber.SubscriptionStatus.ACTIVE).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<NewsletterSubscriberDTO> getActiveSubscribers(Pageable pageable) {
        return subscriberRepository.findAllActiveSubscribers(NewsletterSubscriber.SubscriptionStatus.ACTIVE, pageable).map(this::convertToDTO);
    }

    public NewsletterSubscriberDTO getSubscriberById(Long id) {
        return subscriberRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Subscriber not found"));
    }

    public List<SubscriptionAuditLog> getSubscriberAuditLog(Long subscriberId) {
        return auditLogRepository.findBySubscriberIdOrderByCreatedAtDesc(subscriberId);
    }

    public long getSubscriberCount(NewsletterSubscriber.SubscriptionStatus status) {
        return subscriberRepository.countByStatus(status);
    }

    private void logAudit(Long subscriberId, String email, SubscriptionAuditLog.AuditAction action,
                         NewsletterSubscriber.SubscriptionStatus oldStatus,
                         NewsletterSubscriber.SubscriptionStatus newStatus,
                         String reason, String ipAddress, String userAgent,
                         String performedBy, Long adminId) {
        SubscriptionAuditLog log = SubscriptionAuditLog.builder()
                .subscriberId(subscriberId)
                .subscriberEmail(email)
                .action(action)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .reason(reason)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .performedBy(performedBy)
                .adminId(adminId)
                .build();

        auditLogRepository.save(log);
    }

    private NewsletterSubscriberDTO convertToDTO(NewsletterSubscriber subscriber) {
        return NewsletterSubscriberDTO.builder()
                .id(subscriber.getId())
                .email(subscriber.getEmail())
                .name(subscriber.getName())
                .phone(subscriber.getPhone())
                .status(subscriber.getStatus())
                .subscribedAt(subscriber.getSubscribedAt())
                .unsubscribedAt(subscriber.getUnsubscribedAt())
                .unsubscribeReason(subscriber.getUnsubscribeReason())
                .emailBounced(subscriber.getEmailBounced())
                .bounceCount(subscriber.getBounceCount())
                .lastEmailSentAt(subscriber.getLastEmailSentAt())
                .totalEmailsSent(subscriber.getTotalEmailsSent())
                .source(subscriber.getSource())
                .createdAt(subscriber.getCreatedAt())
                .updatedAt(subscriber.getUpdatedAt())
                .build();
    }

    private String getClientIP(HttpServletRequest request) {
        if (request == null) return null;
        
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader("User-Agent");
    }
}
