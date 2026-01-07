package org.example.monentregratuit.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.monentregratuit.service.EmailBlocklistService;
import org.example.monentregratuit.service.NewsletterSubscriberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/unsubscribe")
@RequiredArgsConstructor
public class PublicUnsubscribeController {

    private final NewsletterSubscriberService subscriberService;
    private final EmailBlocklistService blocklistService;

    @GetMapping("/{token}")
    public ResponseEntity<?> unsubscribeGet(
            @PathVariable String token,
            @RequestParam(required = false) String reason,
            HttpServletRequest request) {
        try {
            subscriberService.unsubscribe(token, reason != null ? reason : "User requested unsubscribe", request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "You have been successfully unsubscribed from our newsletter."
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
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "You have been successfully unsubscribed. You will not receive any further emails from us."
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
                "message", "You have been successfully unsubscribed from our newsletter."
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
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "You have been successfully unsubscribed. You will not receive any further emails from us."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
