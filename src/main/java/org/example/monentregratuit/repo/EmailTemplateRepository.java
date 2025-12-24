package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.EmailTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    List<EmailTemplate> findByIsActiveTrue();
    Page<EmailTemplate> findAll(Pageable pageable);
    Optional<EmailTemplate> findByName(String name);
}
