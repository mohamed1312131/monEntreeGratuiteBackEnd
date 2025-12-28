package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.NewsletterSubscriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, Long> {
    
    Optional<NewsletterSubscriber> findByEmail(String email);
    
    Optional<NewsletterSubscriber> findByUnsubscribeToken(String token);
    
    boolean existsByEmail(String email);
    
    List<NewsletterSubscriber> findByStatus(NewsletterSubscriber.SubscriptionStatus status);
    
    Page<NewsletterSubscriber> findByStatus(NewsletterSubscriber.SubscriptionStatus status, Pageable pageable);
    
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE " +
           "(:status IS NULL OR ns.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(ns.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(ns.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:dateFrom IS NULL OR ns.subscribedAt >= :dateFrom) AND " +
           "(:dateTo IS NULL OR ns.subscribedAt <= :dateTo) " +
           "ORDER BY ns.subscribedAt DESC")
    Page<NewsletterSubscriber> findByFilters(
        @Param("status") NewsletterSubscriber.SubscriptionStatus status,
        @Param("search") String search,
        @Param("dateFrom") LocalDateTime dateFrom,
        @Param("dateTo") LocalDateTime dateTo,
        Pageable pageable
    );
    
    @Query("SELECT COUNT(ns) FROM NewsletterSubscriber ns WHERE ns.status = :status")
    long countByStatus(@Param("status") NewsletterSubscriber.SubscriptionStatus status);
    
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status = :status AND ns.emailBounced = false")
    List<NewsletterSubscriber> findAllActiveSubscribers(@Param("status") NewsletterSubscriber.SubscriptionStatus status);
    
    @Query("SELECT ns FROM NewsletterSubscriber ns WHERE ns.status = :status AND ns.emailBounced = false")
    Page<NewsletterSubscriber> findAllActiveSubscribers(@Param("status") NewsletterSubscriber.SubscriptionStatus status, Pageable pageable);
}
