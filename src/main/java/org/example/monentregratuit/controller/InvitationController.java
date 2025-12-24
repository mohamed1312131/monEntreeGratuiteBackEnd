package org.example.monentregratuit.controller;

import org.example.monentregratuit.DTO.*;
import org.example.monentregratuit.service.InvitationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/invitations")
@CrossOrigin(origins = "${app.frontend.url}", allowCredentials = "true", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class InvitationController {

    @Autowired
    private InvitationService invitationService;

    @PostMapping("/upload")
    public ResponseEntity<InvitationUploadResponse> uploadExcelFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "foireId", required = false) Long foireId) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(InvitationUploadResponse.builder()
                            .success(false)
                            .message("Fichier vide")
                            .totalRows(0)
                            .successfulRows(0)
                            .failedRows(0)
                            .errors(java.util.List.of("Fichier vide"))
                            .build());
        }

        String filename = file.getOriginalFilename();
        if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(InvitationUploadResponse.builder()
                            .success(false)
                            .message("Format de fichier invalide. Seuls .xlsx et .xls sont acceptés")
                            .totalRows(0)
                            .successfulRows(0)
                            .failedRows(0)
                            .errors(java.util.List.of("Format de fichier invalide"))
                            .build());
        }

        InvitationUploadResponse response = invitationService.uploadExcelFileWithFoire(file, foireId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<InvitationDTO>> getAllInvitations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) Boolean emailSent,
            @RequestParam(required = false) Long foireId) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<InvitationDTO> invitations;
        if (foireId != null) {
            invitations = invitationService.getInvitationsByFoire(foireId, pageable);
        } else if (emailSent != null) {
            invitations = invitationService.getInvitationsByEmailSentStatus(emailSent, pageable);
        } else {
            invitations = invitationService.getAllInvitations(pageable);
        }
        
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvitationDTO> getInvitationById(@PathVariable Long id) {
        try {
            InvitationDTO invitation = invitationService.getInvitationById(id);
            return ResponseEntity.ok(invitation);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<EmailPreviewResponse> previewEmail(@RequestBody EmailPreviewRequest request) {
        try {
            EmailPreviewResponse preview = invitationService.previewEmail(request);
            return ResponseEntity.ok(preview);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(EmailPreviewResponse.builder()
                            .subject("")
                            .htmlContent("Erreur: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/send-emails")
    public ResponseEntity<EmailSendResponse> sendEmails(@RequestBody EmailSendRequest request) {
        try {
            EmailSendResponse response = invitationService.sendEmails(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(EmailSendResponse.builder()
                            .success(false)
                            .message("Erreur: " + e.getMessage())
                            .totalEmails(0)
                            .successfulEmails(0)
                            .failedEmails(0)
                            .build());
        }
    }
}
