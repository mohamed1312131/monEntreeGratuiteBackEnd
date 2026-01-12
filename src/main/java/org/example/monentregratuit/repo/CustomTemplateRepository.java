package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.CustomTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomTemplateRepository extends JpaRepository<CustomTemplate, Long> {
    Optional<CustomTemplate> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
