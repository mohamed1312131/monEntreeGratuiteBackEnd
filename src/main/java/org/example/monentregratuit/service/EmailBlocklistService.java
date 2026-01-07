package org.example.monentregratuit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.monentregratuit.entity.EmailBlocklist;
import org.example.monentregratuit.repo.EmailBlocklistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EmailBlocklistService {

    @Autowired
    private EmailBlocklistRepository emailBlocklistRepository;

    public boolean isBlocked(String email) {
        return emailBlocklistRepository.existsByEmail(email.toLowerCase().trim());
    }

    @Transactional
    public EmailBlocklist blockEmail(String email, String reason, HttpServletRequest request) {
        String normalizedEmail = email.toLowerCase().trim();
        
        Optional<EmailBlocklist> existing = emailBlocklistRepository.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            return existing.get();
        }

        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        EmailBlocklist blocklist = EmailBlocklist.builder()
                .email(normalizedEmail)
                .reason(reason != null ? reason : "User unsubscribed")
                .blockedAt(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        return emailBlocklistRepository.save(blocklist);
    }

    @Transactional
    public void unblockEmail(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        emailBlocklistRepository.findByEmail(normalizedEmail)
                .ifPresent(emailBlocklistRepository::delete);
    }

    public List<EmailBlocklist> getAllBlockedEmails() {
        return emailBlocklistRepository.findAll();
    }

    public Optional<EmailBlocklist> getBlockedEmail(String email) {
        return emailBlocklistRepository.findByEmail(email.toLowerCase().trim());
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
