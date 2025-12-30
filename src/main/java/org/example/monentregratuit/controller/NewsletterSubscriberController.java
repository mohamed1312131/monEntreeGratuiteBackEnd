package org.example.monentregratuit.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.monentregratuit.DTO.NewsletterSubscriberDTO;
import org.example.monentregratuit.entity.NewsletterSubscriber;
import org.example.monentregratuit.entity.SubscriptionAuditLog;
import org.example.monentregratuit.service.NewsletterSubscriberService;
import org.example.monentregratuit.service.RecaptchaService;
import org.example.monentregratuit.util.InputSanitizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/newsletter-subscribers")
@RequiredArgsConstructor
public class NewsletterSubscriberController {

    private final NewsletterSubscriberService subscriberService;
    private final RecaptchaService recaptchaService;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody Map<String, String> request, HttpServletRequest httpRequest) {
        try {
            // Validate reCAPTCHA if token is provided
            String recaptchaToken = request.get("recaptchaToken");
            if (recaptchaToken != null && !recaptchaToken.isEmpty()) {
                if (!recaptchaService.verifyRecaptcha(recaptchaToken)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "reCAPTCHA verification failed. Please try again."));
                }
            }

            String email = request.get("email");
            String name = request.get("name");
            String phone = request.get("phone");
            String source = request.get("source");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            // Sanitize and validate email
            email = InputSanitizer.sanitizeEmail(email);
            if (!InputSanitizer.isValidEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid email format"));
            }
            if (InputSanitizer.isDisposableEmail(email)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Disposable email addresses are not allowed"));
            }

            // Sanitize other inputs
            if (name != null) {
                name = InputSanitizer.sanitizeName(name);
            }
            if (phone != null) {
                phone = InputSanitizer.sanitizePhone(phone);
            }

            NewsletterSubscriberDTO subscriber = subscriberService.subscribe(email, name, phone, source, httpRequest);
            return ResponseEntity.ok(subscriber);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<Page<NewsletterSubscriberDTO>> getAllSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "subscribedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(subscriberService.getAllSubscribers(pageable));
    }

    @GetMapping("/active")
    public ResponseEntity<Page<NewsletterSubscriberDTO>> getActiveSubscribers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("subscribedAt").descending());
        return ResponseEntity.ok(subscriberService.getActiveSubscribers(pageable));
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchSubscribers(
            @RequestParam(required = false) NewsletterSubscriber.SubscriptionStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            // Default sort by subscribedAt DESC
            Pageable pageable = PageRequest.of(page, size, Sort.by("subscribedAt").descending());
            
            return ResponseEntity.ok(subscriberService.searchSubscribers(status, search, pageable));
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace to server logs
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage(), "cause", e.getCause() != null ? e.getCause().getMessage() : "Unknown"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsletterSubscriberDTO> getSubscriberById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(subscriberService.getSubscriberById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/audit-log")
    public ResponseEntity<List<SubscriptionAuditLog>> getSubscriberAuditLog(@PathVariable Long id) {
        return ResponseEntity.ok(subscriberService.getSubscriberAuditLog(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSubscriber(
            @PathVariable Long id,
            @RequestBody NewsletterSubscriberDTO dto,
            @RequestHeader(value = "Admin-Id", required = false) Long adminId) {
        try {
            NewsletterSubscriberDTO updated = subscriberService.updateSubscriber(id, dto, adminId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSubscriber(
            @PathVariable Long id,
            @RequestHeader(value = "Admin-Id", required = false) Long adminId) {
        try {
            subscriberService.deleteSubscriber(id, adminId);
            return ResponseEntity.ok(Map.of("message", "Subscriber deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("active", subscriberService.getSubscriberCount(NewsletterSubscriber.SubscriptionStatus.ACTIVE));
        stats.put("unsubscribed", subscriberService.getSubscriberCount(NewsletterSubscriber.SubscriptionStatus.UNSUBSCRIBED));
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/unsubscribe-by-email")
    public ResponseEntity<?> unsubscribeByEmail(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        try {
            String email = request.get("email");
            String reason = request.get("reason");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }

            subscriberService.unsubscribeByEmail(email, reason, httpRequest);
            return ResponseEntity.ok(Map.of("message", "Successfully unsubscribed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send-bulk-email")
    public ResponseEntity<?> sendBulkEmail(
            @RequestBody Map<String, Object> request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // CRITICAL SECURITY: This endpoint MUST be authenticated
            // The SecurityConfig should handle this, but we add an extra check
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "Authentication required for bulk email operations"));
            }

            @SuppressWarnings("unchecked")
            List<Long> subscriberIds = (List<Long>) request.get("subscriberIds");
            Long templateId = Long.valueOf(request.get("templateId").toString());

            if (subscriberIds == null || subscriberIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Subscriber IDs are required"));
            }

            if (templateId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Template ID is required"));
            }

            // Limit bulk email to reasonable size to prevent abuse
            if (subscriberIds.size() > 1000) {
                return ResponseEntity.badRequest().body(Map.of("error", "Cannot send to more than 1000 subscribers at once"));
            }

            Map<String, Object> result = subscriberService.sendBulkEmail(subscriberIds, templateId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
