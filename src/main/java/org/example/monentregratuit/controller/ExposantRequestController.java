package org.example.monentregratuit.controller;

import lombok.AllArgsConstructor;
import org.example.monentregratuit.entity.ExposantRequest;
import org.example.monentregratuit.service.ExposantRequestService;
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
    public ResponseEntity<?> createExposantRequest(@RequestBody ExposantRequest exposantRequest) {
        try {
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
