package org.example.monentregratuit.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.monentregratuit.entity.EmailBlocklist;
import org.example.monentregratuit.service.EmailBlocklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/email-blocklist")
@RequiredArgsConstructor
public class EmailBlocklistController {

    private final EmailBlocklistService blocklistService;

    @GetMapping
    public ResponseEntity<List<EmailBlocklist>> getAllBlockedEmails() {
        return ResponseEntity.ok(blocklistService.getAllBlockedEmails());
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean isBlocked = blocklistService.isBlocked(email);
        return ResponseEntity.ok(Map.of(
            "email", email,
            "isBlocked", isBlocked
        ));
    }

    @PostMapping("/block")
    public ResponseEntity<?> blockEmail(
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
            
            EmailBlocklist blocked = blocklistService.blockEmail(email, reason, request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email blocked successfully",
                "data", blocked
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/unblock")
    public ResponseEntity<?> unblockEmail(@RequestParam String email) {
        try {
            blocklistService.unblockEmail(email);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Email unblocked successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
