package org.example.monentregratuit.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.monentregratuit.entity.EmailLog;
import org.example.monentregratuit.repo.EmailLogRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@RestController
@RequestMapping("/api/track")
public class TrackingController {

    private final EmailLogRepository emailLogRepository;

    // 1x1 transparent GIF
    private static final byte[] PIXEL_BYTES = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");

    public TrackingController(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
    }

    @GetMapping("/open")
    public void trackOpen(@RequestParam("token") String token, HttpServletResponse response) {
        try {
            Optional<EmailLog> logOpt = emailLogRepository.findByTrackingToken(token);
            if (logOpt.isPresent()) {
                EmailLog log = logOpt.get();
                if (!log.isOpened()) {
                    log.setOpened(true);
                    log.setOpenedAt(LocalDateTime.now());
                    emailLogRepository.save(log);
                }
            }

            response.setContentType("image/gif");
            response.getOutputStream().write(PIXEL_BYTES);
            response.flushBuffer();
        } catch (Exception e) {
            // Ignore errors for tracking pixel
        }
    }

    @GetMapping("/click")
    public RedirectView trackClick(@RequestParam("token") String token, @RequestParam("target") String targetUrl) {
        try {
            Optional<EmailLog> logOpt = emailLogRepository.findByTrackingToken(token);
            if (logOpt.isPresent()) {
                EmailLog log = logOpt.get();
                log.setClicked(true);
                if (log.getClickedAt() == null) {
                    log.setClickedAt(LocalDateTime.now());
                }
                log.setClickCount(log.getClickCount() + 1);
                emailLogRepository.save(log);
            }
        } catch (Exception e) {
            // Log error but ensure redirect happens
            System.err.println("Error tracking click: " + e.getMessage());
        }

        return new RedirectView(targetUrl);
    }
}
