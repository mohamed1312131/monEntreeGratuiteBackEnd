package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.SubscriptionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionAuditLogRepository extends JpaRepository<SubscriptionAuditLog, Long> {
    
    List<SubscriptionAuditLog> findBySubscriberIdOrderByCreatedAtDesc(Long subscriberId);
    
    Page<SubscriptionAuditLog> findBySubscriberIdOrderByCreatedAtDesc(Long subscriberId, Pageable pageable);
    
    List<SubscriptionAuditLog> findBySubscriberEmailOrderByCreatedAtDesc(String email);
    
    Page<SubscriptionAuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
