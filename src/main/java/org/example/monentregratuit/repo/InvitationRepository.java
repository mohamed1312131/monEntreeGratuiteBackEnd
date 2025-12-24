package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.Invitation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    boolean existsByEmail(String email);
    boolean existsByCode(String code);
    Page<Invitation> findByEmailSent(Boolean emailSent, Pageable pageable);
    Page<Invitation> findByFoireId(Long foireId, Pageable pageable);
    List<Invitation> findByIdIn(List<Long> ids);
}
