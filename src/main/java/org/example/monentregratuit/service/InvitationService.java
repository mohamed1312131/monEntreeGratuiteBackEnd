package org.example.monentregratuit.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.monentregratuit.DTO.*;
import org.example.monentregratuit.entity.EmailTemplate;
import org.example.monentregratuit.entity.Invitation;
import org.example.monentregratuit.repo.EmailTemplateRepository;
import org.example.monentregratuit.repo.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class InvitationService {

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private org.example.monentregratuit.repo.FoireRepository foireRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    public InvitationUploadResponse uploadExcelFile(MultipartFile file) {
        return uploadExcelFileWithFoire(file, null);
    }

    public InvitationUploadResponse uploadExcelFileWithFoire(MultipartFile file, Long foireId) {
        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int successfulRows = 0;
        int failedRows = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            int headerRowIndex = -1;
            for (int i = 0; i <= 5; i++) {
                Row row = sheet.getRow(i);
                if (row != null && isHeaderRow(row)) {
                    headerRowIndex = i;
                    break;
                }
            }

            if (headerRowIndex == -1) {
                return InvitationUploadResponse.builder()
                        .success(false)
                        .message("En-tête non trouvé. Colonnes requises: Nom, Email, DATE, HEURE, CODE")
                        .totalRows(0)
                        .successfulRows(0)
                        .failedRows(0)
                        .errors(List.of("En-tête non trouvé"))
                        .build();
            }

            for (int i = headerRowIndex + 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                totalRows++;
                try {
                    String nom = getCellValueAsString(row.getCell(0));
                    String email = getCellValueAsString(row.getCell(1));
                    String date = getCellValueAsString(row.getCell(2));
                    String heure = getCellValueAsString(row.getCell(3));
                    String code = getCellValueAsString(row.getCell(4));

                    if (nom == null || nom.trim().isEmpty()) {
                        errors.add("Ligne " + (i + 1) + ": Nom requis");
                        failedRows++;
                        continue;
                    }

                    if (email == null || email.trim().isEmpty()) {
                        errors.add("Ligne " + (i + 1) + ": Email requis");
                        failedRows++;
                        continue;
                    }

                    if (!EMAIL_PATTERN.matcher(email).matches()) {
                        errors.add("Ligne " + (i + 1) + ": Format d'email invalide - " + email);
                        failedRows++;
                        continue;
                    }

                    if (invitationRepository.existsByEmail(email)) {
                        errors.add("Ligne " + (i + 1) + ": Email déjà existant - " + email);
                        failedRows++;
                        continue;
                    }

                    if (code != null && !code.trim().isEmpty() && invitationRepository.existsByCode(code)) {
                        errors.add("Ligne " + (i + 1) + ": Code déjà existant - " + code);
                        failedRows++;
                        continue;
                    }

                    Invitation.InvitationBuilder builder = Invitation.builder()
                            .nom(nom.trim())
                            .email(email.trim().toLowerCase())
                            .date(date)
                            .heure(heure)
                            .code(code)
                            .emailSent(false);

                    if (foireId != null) {
                        org.example.monentregratuit.entity.Foire foire = foireRepository.findById(foireId)
                                .orElseThrow(() -> new RuntimeException("Foire not found with id: " + foireId));
                        builder.foire(foire);
                    }

                    Invitation invitation = builder.build();
                    invitationRepository.save(invitation);
                    successfulRows++;

                } catch (Exception e) {
                    errors.add("Ligne " + (i + 1) + ": " + e.getMessage());
                    failedRows++;
                }
            }

            String message = successfulRows > 0
                    ? successfulRows + " invitation(s) importée(s) avec succès"
                    : "Aucune invitation importée";

            return InvitationUploadResponse.builder()
                    .success(successfulRows > 0)
                    .message(message)
                    .totalRows(totalRows)
                    .successfulRows(successfulRows)
                    .failedRows(failedRows)
                    .errors(errors)
                    .build();

        } catch (IOException e) {
            return InvitationUploadResponse.builder()
                    .success(false)
                    .message("Erreur lors de la lecture du fichier: " + e.getMessage())
                    .totalRows(0)
                    .successfulRows(0)
                    .failedRows(0)
                    .errors(List.of(e.getMessage()))
                    .build();
        }
    }

    public Page<InvitationDTO> getAllInvitations(Pageable pageable) {
        return invitationRepository.findAll(pageable).map(this::convertToDTO);
    }

    public Page<InvitationDTO> getInvitationsByEmailSentStatus(Boolean emailSent, Pageable pageable) {
        return invitationRepository.findByEmailSent(emailSent, pageable).map(this::convertToDTO);
    }

    public InvitationDTO getInvitationById(Long id) {
        Invitation invitation = invitationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invitation not found with id: " + id));
        return convertToDTO(invitation);
    }

    public EmailPreviewResponse previewEmail(EmailPreviewRequest request) {
        EmailTemplate template = emailTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        Invitation invitation = invitationRepository.findById(request.getInvitationId())
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        String processedSubject = emailTemplateService.replaceDynamicFields(template.getSubject(), invitation);
        String processedContent = emailTemplateService.replaceDynamicFields(template.getHtmlContent(), invitation);

        return EmailPreviewResponse.builder()
                .subject(processedSubject)
                .htmlContent(processedContent)
                .recipientName(invitation.getNom())
                .recipientEmail(invitation.getEmail())
                .build();
    }

    @Transactional
    public EmailSendResponse sendEmails(EmailSendRequest request) {
        EmailTemplate template = emailTemplateRepository.findById(request.getTemplateId())
                .orElseThrow(() -> new RuntimeException("Template not found"));

        List<Invitation> invitations = invitationRepository.findByIdIn(request.getInvitationIds());

        if (invitations.isEmpty()) {
            return EmailSendResponse.builder()
                    .success(false)
                    .message("Aucune invitation trouvée")
                    .totalEmails(0)
                    .successfulEmails(0)
                    .failedEmails(0)
                    .build();
        }

        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Invitation invitation : invitations) {
            try {
                String processedSubject = emailTemplateService.replaceDynamicFields(template.getSubject(), invitation);
                String processedContent = emailTemplateService.replaceDynamicFields(template.getHtmlContent(), invitation);

                sendEmail(invitation.getEmail(), processedSubject, processedContent);

                invitation.setEmailSent(true);
                invitation.setSentAt(LocalDateTime.now());
                invitationRepository.save(invitation);

                successCount++;

            } catch (Exception e) {
                failCount++;
                errors.add(invitation.getEmail() + ": " + e.getMessage());
            }
        }

        String message = successCount + " email(s) envoyé(s) avec succès";
        if (failCount > 0) {
            message += ", " + failCount + " échec(s)";
        }

        return EmailSendResponse.builder()
                .success(successCount > 0)
                .message(message)
                .totalEmails(invitations.size())
                .successfulEmails(successCount)
                .failedEmails(failCount)
                .build();
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private boolean isHeaderRow(Row row) {
        if (row == null) return false;
        
        String firstCell = getCellValueAsString(row.getCell(0));
        return firstCell != null && 
               (firstCell.equalsIgnoreCase("Nom") || firstCell.equalsIgnoreCase("Name"));
    }

    private boolean isRowEmpty(Row row) {
        for (int i = 0; i < 5; i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellValueAsString(cell);
                if (value != null && !value.trim().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private InvitationDTO convertToDTO(Invitation invitation) {
        InvitationDTO.InvitationDTOBuilder builder = InvitationDTO.builder()
                .id(invitation.getId())
                .nom(invitation.getNom())
                .email(invitation.getEmail())
                .date(invitation.getDate())
                .heure(invitation.getHeure())
                .code(invitation.getCode())
                .emailSent(invitation.getEmailSent())
                .sentAt(invitation.getSentAt())
                .createdAt(invitation.getCreatedAt());

        if (invitation.getFoire() != null) {
            builder.foireId(invitation.getFoire().getId())
                   .foireName(invitation.getFoire().getName());
        }

        return builder.build();
    }

    public Page<InvitationDTO> getInvitationsByFoire(Long foireId, Pageable pageable) {
        return invitationRepository.findByFoireId(foireId, pageable).map(this::convertToDTO);
    }
}
