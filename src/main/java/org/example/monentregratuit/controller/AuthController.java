package org.example.monentregratuit.controller;

import org.example.monentregratuit.entity.Admin;
import org.example.monentregratuit.entity.LoginRequest;
import org.example.monentregratuit.service.AdminAuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminAuthService adminAuthService;

    public AuthController(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest loginRequest) {
        try {
            Map<String, String> response = adminAuthService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<String> createAdminAccount(@RequestBody Admin admin) {
        try {
            adminAuthService.createAdminAccount(admin);
            return ResponseEntity.ok("Admin account created successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}