package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    Optional<EmailLog> findByTrackingToken(String trackingToken);
    List<EmailLog> findByCampaignId(Long campaignId);
    void deleteByCampaignId(Long campaignId);
    
    long countByCampaignIdAndStatus(Long campaignId, EmailLog.EmailStatus status);
    long countByCampaignIdAndOpenedTrue(Long campaignId);
    long countByCampaignIdAndClickedTrue(Long campaignId);
    
    List<EmailLog> findByExcelUserId(Long excelUserId);
}
