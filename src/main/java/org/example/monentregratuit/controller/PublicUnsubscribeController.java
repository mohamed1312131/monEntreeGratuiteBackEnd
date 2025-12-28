package org.example.monentregratuit.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.monentregratuit.service.NewsletterSubscriberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/unsubscribe")
@RequiredArgsConstructor
public class PublicUnsubscribeController {

    private final NewsletterSubscriberService subscriberService;

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
}
