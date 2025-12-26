package org.example.monentregratuit.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.monentregratuit.entity.UserVisit;
import org.example.monentregratuit.service.UserVisitService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/visits")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class UserVisitController {
    private final UserVisitService service;

    public UserVisitController(UserVisitService service) {
        this.service = service;
    }

    @PostMapping("/track")
    public void trackUserVisit(HttpServletRequest request) {
        String ip = getClientIp(request);
        service.trackVisit(ip);
    }

    @GetMapping
    public List<UserVisit> getAllVisits() {
        return service.getAllVisits();
    }

    @GetMapping("/stats/by-country")
    public Map<String, Long> getVisitsByCountry() {
        return service.getVisitsByCountry();
    }

    @GetMapping("/stats/by-country/{year}")
    public Map<String, Long> getVisitsByCountryAndYear(@PathVariable int year) {
        return service.getVisitsByCountryAndYear(year);
    }

    @GetMapping("/stats/total")
    public Map<String, Long> getTotalVisits() {
        Map<String, Long> response = new HashMap<>();
        response.put("totalVisits", service.getTotalVisits());
        return response;
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
