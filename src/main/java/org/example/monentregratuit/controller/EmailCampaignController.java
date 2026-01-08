package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.CampaignStatsDTO;
import org.example.monentregratuit.DTO.EmailLogUserDTO;
import org.example.monentregratuit.entity.EmailCampaign;
import org.example.monentregratuit.service.EmailCampaignService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campaigns")
public class EmailCampaignController {

    private final EmailCampaignService campaignService;

    public EmailCampaignController(EmailCampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @GetMapping("/foire/{foireId}")
    public ResponseEntity<List<EmailCampaign>> getCampaignsByFoire(@PathVariable Long foireId) {
        return ResponseEntity.ok(campaignService.getCampaignsByFoire(foireId));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<CampaignStatsDTO> getCampaignStats(@PathVariable Long id) {
        return ResponseEntity.ok(campaignService.getCampaignStats(id));
    }

    @GetMapping("/{id}/users/{type}")
    public ResponseEntity<List<EmailLogUserDTO>> getCampaignUsers(@PathVariable Long id, @PathVariable String type) {
        return ResponseEntity.ok(campaignService.getCampaignUsersByType(id, type));
    }
}
