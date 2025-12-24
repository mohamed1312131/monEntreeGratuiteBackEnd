package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.ExposantRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExposantRequestRepository extends JpaRepository<ExposantRequest, Long> {
    
    List<ExposantRequest> findByStatus(String status);
    
    List<ExposantRequest> findByOrderByCreatedAtDesc();
    
    Optional<ExposantRequest> findByEmail(String email);
    
    Optional<ExposantRequest> findByTelephone(String telephone);
}
