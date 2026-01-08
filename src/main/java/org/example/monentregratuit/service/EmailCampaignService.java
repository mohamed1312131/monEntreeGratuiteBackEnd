package org.example.monentregratuit.service;

import org.example.monentregratuit.DTO.CampaignStatsDTO;
import org.example.monentregratuit.DTO.EmailLogUserDTO;
import org.example.monentregratuit.entity.EmailCampaign;
import org.example.monentregratuit.entity.EmailLog;
import org.example.monentregratuit.repo.EmailBlocklistRepository;
import org.example.monentregratuit.repo.EmailCampaignRepository;
import org.example.monentregratuit.repo.EmailLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EmailCampaignService {

    private final EmailCampaignRepository campaignRepository;
    private final EmailLogRepository logRepository;
    private final EmailBlocklistRepository blocklistRepository;

    public EmailCampaignService(EmailCampaignRepository campaignRepository,
                                EmailLogRepository logRepository,
                                EmailBlocklistRepository blocklistRepository) {
        this.campaignRepository = campaignRepository;
        this.logRepository = logRepository;
        this.blocklistRepository = blocklistRepository;
    }

    public List<EmailCampaign> getCampaignsByFoire(Long foireId) {
        return campaignRepository.findByFoireIdOrderBySentAtDesc(foireId);
    }

    public CampaignStatsDTO getCampaignStats(Long campaignId) {
        EmailCampaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new RuntimeException("Campaign not found"));

        List<EmailLog> logs = logRepository.findByCampaignId(campaignId);

        int total = logs.size();
        int delivered = 0;
        int failed = 0;
        int opened = 0;
        int clicked = 0;

        // Collect emails to check for unsubscribe
        Set<String> recipientEmails = logs.stream()
                .map(EmailLog::getRecipientEmail)
                .collect(Collectors.toSet());

        for (EmailLog log : logs) {
            if (log.getStatus() == EmailLog.EmailStatus.SENT) {
                delivered++;
            } else {
                failed++;
            }

            if (log.isOpened()) {
                opened++;
            }

            if (log.isClicked()) {
                clicked++;
            }
        }

        // Calculate unsubscribes
        // We count how many of the recipients are currently in the blocklist
        // Note: This is a current snapshot. If they unsubscribed BEFORE the campaign, 
        // they shouldn't have received it (filtered by sender service), 
        // so this largely reflects those who unsubscribed AFTER.
        int unsubscribed = 0;
        if (!recipientEmails.isEmpty()) {
            // Using a loop here as we haven't added findByEmailIn to repo yet
            // For larger datasets, we should update the repository
            for (String email : recipientEmails) {
                if (blocklistRepository.existsByEmail(email)) {
                    unsubscribed++;
                }
            }
        }

        return CampaignStatsDTO.builder()
                .id(campaign.getId())
                .name(campaign.getName())
                .sentAt(campaign.getSentAt())
                .totalRecipients(total)
                .deliveredCount(delivered)
                .failedCount(failed)
                .openCount(opened)
                .clickCount(clicked)
                .unsubscribeCount(unsubscribed)
                .spamCount(0) // No feedback loop integration yet
                .build();
    }

    public List<EmailLogUserDTO> getCampaignUsersByType(Long campaignId, String type) {
        List<EmailLog> logs = logRepository.findByCampaignId(campaignId);
        
        return logs.stream()
                .filter(log -> matchesType(log, type))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean matchesType(EmailLog log, String type) {
        switch (type) {
            case "all":
                return true;
            case "delivered":
                return log.getStatus() == EmailLog.EmailStatus.SENT;
            case "failed":
                return log.getStatus() == EmailLog.EmailStatus.FAILED;
            case "opened":
                return log.isOpened();
            case "not-opened":
                return log.getStatus() == EmailLog.EmailStatus.SENT && !log.isOpened();
            case "clicked":
                return log.isClicked();
            case "not-clicked":
                return log.isOpened() && !log.isClicked();
            case "unsubscribed":
                return blocklistRepository.existsByEmail(log.getRecipientEmail());
            default:
                return false;
        }
    }

    private EmailLogUserDTO convertToDTO(EmailLog log) {
        String recipientName = log.getExcelUser() != null ? log.getExcelUser().getNom() : "N/A";
        
        return EmailLogUserDTO.builder()
                .id(log.getId())
                .recipientEmail(log.getRecipientEmail())
                .recipientName(recipientName)
                .status(log.getStatus().name())
                .opened(log.isOpened())
                .openedAt(log.getOpenedAt())
                .clicked(log.isClicked())
                .clickedAt(log.getClickedAt())
                .clickCount(log.getClickCount())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}
