package org.example.monentregratuit.service;

import lombok.AllArgsConstructor;
import org.example.monentregratuit.entity.ExposantRequest;
import org.example.monentregratuit.repo.ExposantRequestRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ExposantRequestService {

    private final ExposantRequestRepository exposantRequestRepository;

    public List<ExposantRequest> getAllExposantRequests() {
        return exposantRequestRepository.findByOrderByCreatedAtDesc();
    }

    public List<ExposantRequest> getExposantRequestsByStatus(String status) {
        return exposantRequestRepository.findByStatus(status);
    }

    public Optional<ExposantRequest> getExposantRequestById(Long id) {
        return exposantRequestRepository.findById(id);
    }

    public ExposantRequest createExposantRequest(ExposantRequest exposantRequest) {
        // Check if email already exists
        Optional<ExposantRequest> existingByEmail = exposantRequestRepository.findByEmail(exposantRequest.getEmail());
        if (existingByEmail.isPresent()) {
            throw new RuntimeException("Une demande existe déjà avec cette adresse email.");
        }

        // Check if telephone already exists
        Optional<ExposantRequest> existingByPhone = exposantRequestRepository.findByTelephone(exposantRequest.getTelephone());
        if (existingByPhone.isPresent()) {
            throw new RuntimeException("Une demande existe déjà avec ce numéro de téléphone.");
        }

        exposantRequest.setStatus("PENDING");
        return exposantRequestRepository.save(exposantRequest);
    }

    public ExposantRequest updateExposantRequestStatus(Long id, String status) {
        ExposantRequest exposantRequest = exposantRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Demande exposant introuvable avec l'ID: " + id));
        
        exposantRequest.setStatus(status);
        return exposantRequestRepository.save(exposantRequest);
    }

    public void deleteExposantRequest(Long id) {
        if (!exposantRequestRepository.existsById(id)) {
            throw new RuntimeException("Demande exposant introuvable avec l'ID: " + id);
        }
        exposantRequestRepository.deleteById(id);
    }
}
