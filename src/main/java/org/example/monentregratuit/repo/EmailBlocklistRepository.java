package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.EmailBlocklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailBlocklistRepository extends JpaRepository<EmailBlocklist, Long> {
    Optional<EmailBlocklist> findByEmail(String email);
    boolean existsByEmail(String email);
}
