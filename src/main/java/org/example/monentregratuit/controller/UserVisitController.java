package org.example.monentregratuit.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.monentregratuit.service.UserVisitService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/track-visit")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class UserVisitController {
    private final UserVisitService service;

    public UserVisitController(UserVisitService service) {
        this.service = service;
    }

    @PostMapping
    public void trackUserVisit(HttpServletRequest request) {
        String ip = getClientIp(request);
        service.trackVisit(ip);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For"); // Check proxy header
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP"); // Another proxy header
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr(); // Fallback to default
        }
        return ip.split(",")[0].trim(); // If multiple IPs, take the first one
    }
}
