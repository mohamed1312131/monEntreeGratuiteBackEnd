package org.example.monentregratuit.service;

import org.example.monentregratuit.entity.Admin;
import org.example.monentregratuit.repo.AdminRepository;
import org.example.monentregratuit.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminAuthService {
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AdminAuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, String> authenticate(String email, String rawPassword) {
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);

        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            if (passwordEncoder.matches(rawPassword, admin.getPassword())) {
                String token = jwtUtil.generateToken(admin.getUsername(), admin.getEmail());
                Map<String, String> response = new HashMap<>();
                response.put("token", token);
                response.put("username", admin.getUsername());
                response.put("email", admin.getEmail());
                return response;
            }
        }
        throw new RuntimeException("Invalid credentials");
    }

    public Admin createAdminAccount(Admin admin) {
        // Check if email or username already exists
        if (adminRepository.existsByEmail(admin.getEmail())) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (adminRepository.existsByUsername(admin.getUsername())) {
            throw new IllegalArgumentException("An account with this username already exists.");
        }

        // Create and populate new Admin entity
        Admin newAdmin = new Admin();
        newAdmin.setEmail(admin.getEmail());
        // Hash the password before saving
        newAdmin.setPassword(passwordEncoder.encode(admin.getPassword()));
        newAdmin.setUsername(admin.getUsername());
        newAdmin.setPhoneNumber(admin.getPhoneNumber());

        // Save to database
        return adminRepository.save(newAdmin);
    }
}