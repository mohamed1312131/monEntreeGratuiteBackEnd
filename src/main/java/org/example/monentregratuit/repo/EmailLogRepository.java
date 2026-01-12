package org.example.monentregratuit.repo;

import org.example.monentregratuit.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Modifying
    @Transactional
    @Query("UPDATE EmailLog e SET e.excelUser = NULL WHERE e.excelUser.id = :excelUserId")
    void nullifyExcelUserReference(@Param("excelUserId") Long excelUserId);
}
