package org.example.monentregratuit.controller;

import lombok.AllArgsConstructor;
import org.example.monentregratuit.entity.ExposantRequest;
import org.example.monentregratuit.service.ExposantRequestService;
import org.example.monentregratuit.service.RecaptchaService;
import org.example.monentregratuit.util.InputSanitizer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exposant-requests")
@AllArgsConstructor
public class ExposantRequestController {

    private final ExposantRequestService exposantRequestService;
    private final RecaptchaService recaptchaService;

    @GetMapping
    public ResponseEntity<List<ExposantRequest>> getAllExposantRequests() {
        List<ExposantRequest> requests = exposantRequestService.getAllExposantRequests();
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ExposantRequest>> getExposantRequestsByStatus(@PathVariable String status) {
        List<ExposantRequest> requests = exposantRequestService.getExposantRequestsByStatus(status);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExposantRequest> getExposantRequestById(@PathVariable Long id) {
        return exposantRequestService.getExposantRequestById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createExposantRequest(@RequestBody Map<String, String> request) {
        try {
            // Validate reCAPTCHA if token is provided
            String recaptchaToken = request.get("recaptchaToken");
            if (recaptchaToken != null && !recaptchaToken.isEmpty()) {
                if (!recaptchaService.verifyRecaptcha(recaptchaToken)) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "reCAPTCHA verification failed. Please try again.");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
            }

            // Create ExposantRequest object
            ExposantRequest exposantRequest = new ExposantRequest();
            
            // Sanitize and validate inputs
            String email = request.get("email");
            if (email != null) {
                email = InputSanitizer.sanitizeEmail(email);
                if (!InputSanitizer.isValidEmail(email)) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid email format");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                if (InputSanitizer.isDisposableEmail(email)) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Disposable email addresses are not allowed");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                exposantRequest.setEmail(email);
            }

            String nomPrenom = request.get("nomPrenom");
            if (nomPrenom != null) {
                exposantRequest.setNomPrenom(InputSanitizer.sanitizeName(nomPrenom));
            }

            String telephone = request.get("telephone");
            if (telephone != null) {
                String phone = InputSanitizer.sanitizePhone(telephone);
                if (!InputSanitizer.isValidPhone(phone)) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Invalid phone number format");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                exposantRequest.setTelephone(phone);
            }

            String nomEtablissement = request.get("nomEtablissement");
            if (nomEtablissement != null) {
                exposantRequest.setNomEtablissement(InputSanitizer.sanitizeName(nomEtablissement));
            }

            String secteurActivite = request.get("secteurActivite");
            if (secteurActivite != null) {
                exposantRequest.setSecteurActivite(InputSanitizer.sanitizeName(secteurActivite));
            }

            String description = request.get("description");
            if (description != null) {
                String sanitizedDescription = InputSanitizer.sanitizeMessage(description);
                if (InputSanitizer.containsSuspiciousContent(sanitizedDescription)) {
                    Map<String, String> errorResponse = new HashMap<>();
                    errorResponse.put("error", "Description contains suspicious content");
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                }
                exposantRequest.setDescription(sanitizedDescription);
            }

            String civilite = request.get("civilite");
            if (civilite != null) {
                exposantRequest.setCivilite(InputSanitizer.sanitizeName(civilite));
            }

            Boolean acceptContact = Boolean.parseBoolean(request.get("acceptContact"));
            exposantRequest.setAcceptContact(acceptContact);

            ExposantRequest created = exposantRequestService.createExposantRequest(exposantRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateExposantRequestStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdate) {
        try {
            String status = statusUpdate.get("status");
            if (status == null || status.isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Le statut est requis");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            ExposantRequest updated = exposantRequestService.updateExposantRequestStatus(id, status);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteExposantRequest(@PathVariable Long id) {
        try {
            exposantRequestService.deleteExposantRequest(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
