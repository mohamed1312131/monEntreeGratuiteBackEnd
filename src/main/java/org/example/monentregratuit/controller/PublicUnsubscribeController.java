package org.example.monentregratuit.controller;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.monentregratuit.repo.ReservationsRepository;
import org.example.monentregratuit.service.EmailBlocklistService;
import org.example.monentregratuit.service.NewsletterSubscriberService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/public/unsubscribe")
@RequiredArgsConstructor
public class PublicUnsubscribeController {

    private final NewsletterSubscriberService subscriberService;
    private final EmailBlocklistService blocklistService;
    private final JavaMailSender mailSender;
    private final ReservationsRepository reservationsRepository;

    @Value("${spring.mail.username}")
    private String mailUsername;

    private static final String ADMIN_EMAIL = "saied.mohamed.mehdi.84@gmail.com";

    @GetMapping("/{token}")
    public ResponseEntity<?> unsubscribeGet(
            @PathVariable String token,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        try {
            subscriberService.unsubscribe(token, reason != null ? reason : "User requested unsubscribe", request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vous avez bien été désinscrit de notre newsletter."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/email")
    public ResponseEntity<?> unsubscribeByEmail(
            @RequestParam String email,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        try {
            blocklistService.blockEmail(email, reason != null ? reason : "User unsubscribed", request);
            sendAdminNotification(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vous avez bien été désinscrit. Vous ne recevrez plus aucun email de notre part."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/{token}")
    public ResponseEntity<?> unsubscribePost(
            @PathVariable String token,
            @RequestBody(required = false) Map<String, String> body,
            HttpServletRequest request) {
        try {
            String reason = body != null ? body.get("reason") : "User requested unsubscribe";
            subscriberService.unsubscribe(token, reason, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vous avez bien été désinscrit de notre newsletter."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/email")
    public ResponseEntity<?> unsubscribeByEmailPost(
            @RequestBody Map<String, String> body,
            HttpServletRequest request) {
        try {
            String email = body.get("email");
            String reason = body.get("reason");
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Email is required"
                ));
            }
            blocklistService.blockEmail(email, reason != null ? reason : "User unsubscribed", request);
            sendAdminNotification(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Vous avez bien été désinscrit. Vous ne recevrez plus aucun email de notre part."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    private void sendAdminNotification(String email) {
        try {
            String phone = reservationsRepository.findFirstByEmailOrderByCreatedAtDesc(email)
                    .map(r -> r.getPhone() != null && !r.getPhone().isEmpty() ? r.getPhone() : "Non renseigné")
                    .orElse("Non trouvé");

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailUsername);
            helper.setTo(ADMIN_EMAIL);
            helper.setSubject("Désinscription - " + email);
            helper.setText(
                "Un utilisateur s'est désinscrit de la liste de diffusion.\n\n" +
                "Email : " + email + "\n" +
                "Téléphone : " + phone + "\n" +
                "Date : " + timestamp,
                false
            );
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send admin unsubscribe notification: " + e.getMessage());
        }
    }
}
