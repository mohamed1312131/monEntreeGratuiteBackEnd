package org.example.monentregratuit.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.example.monentregratuit.DTO.EmailTemplateDTO;
import org.example.monentregratuit.DTO.EmailTemplateImageDTO;
import org.example.monentregratuit.entity.EmailTemplate;
import org.example.monentregratuit.entity.EmailTemplateImage;
import org.example.monentregratuit.entity.Invitation;
import org.example.monentregratuit.repo.EmailTemplateImageRepository;
import org.example.monentregratuit.repo.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class EmailTemplateService {

    @Autowired
    private EmailTemplateRepository emailTemplateRepository;

    @Autowired
    private EmailTemplateImageRepository emailTemplateImageRepository;

    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.username}")
    private String mailUsername;

    @org.springframework.beans.factory.annotation.Value("${spring.mail.password}")
    private String mailPassword;

    public EmailTemplateDTO createTemplate(EmailTemplateDTO dto) {
        EmailTemplate template = EmailTemplate.builder()
                .name(dto.getName())
                .subject(dto.getSubject())
                .htmlContent(dto.getHtmlContent())
                .templateConfig(dto.getTemplateConfig())
                .isActive(true)
                .build();

        EmailTemplate saved = emailTemplateRepository.save(template);
        return convertToDTO(saved);
    }

    public Page<EmailTemplateDTO> getAllTemplates(Pageable pageable) {
        return emailTemplateRepository.findAll(pageable).map(this::convertToDTO);
    }

    public List<EmailTemplateDTO> getActiveTemplates() {
        return emailTemplateRepository.findByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public EmailTemplateDTO getTemplateById(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
        return convertToDTO(template);
    }

    public EmailTemplateDTO updateTemplate(Long id, EmailTemplateDTO dto) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));

        template.setName(dto.getName());
        template.setSubject(dto.getSubject());
        template.setHtmlContent(dto.getHtmlContent());
        template.setTemplateConfig(dto.getTemplateConfig());
        if (dto.getIsActive() != null) {
            template.setIsActive(dto.getIsActive());
        }

        EmailTemplate updated = emailTemplateRepository.save(template);
        return convertToDTO(updated);
    }

    @Transactional
    public void deleteTemplate(Long id) {
        EmailTemplate template = emailTemplateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
        
        // Delete associated images first
        List<EmailTemplateImage> images = emailTemplateImageRepository.findByEmailTemplateId(id);
        emailTemplateImageRepository.deleteAll(images);
        
        // Now delete the template
        emailTemplateRepository.delete(template);
    }

    public EmailTemplateImageDTO uploadImage(Long templateId, MultipartFile file) throws IOException {
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        String imageUrl = uploadResult.get("secure_url").toString();
        
        int maxOrder = emailTemplateImageRepository.findByEmailTemplateIdOrderByImageOrderAsc(templateId)
                .stream()
                .mapToInt(EmailTemplateImage::getImageOrder)
                .max()
                .orElse(-1);

        EmailTemplateImage image = EmailTemplateImage.builder()
                .emailTemplate(template)
                .imageUrl(imageUrl)
                .imageName(file.getOriginalFilename())
                .imageOrder(maxOrder + 1)
                .build();

        EmailTemplateImage saved = emailTemplateImageRepository.save(image);
        return convertImageToDTO(saved);
    }

    public List<EmailTemplateImageDTO> getTemplateImages(Long templateId) {
        return emailTemplateImageRepository.findByEmailTemplateIdOrderByImageOrderAsc(templateId)
                .stream()
                .map(this::convertImageToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteImage(Long templateId, Long imageId) {
        EmailTemplateImage image = emailTemplateImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        if (!image.getEmailTemplate().getId().equals(templateId)) {
            throw new RuntimeException("Image does not belong to this template");
        }

        emailTemplateImageRepository.delete(image);
    }

    public String replaceDynamicFields(String content, Invitation invitation) {
        if (content == null || invitation == null) {
            return content;
        }

        content = replaceField(content, "NOM", invitation.getNom());
        content = replaceField(content, "EMAIL", invitation.getEmail());
        content = replaceField(content, "DATE", invitation.getDate());
        content = replaceField(content, "HEURE", invitation.getHeure());
        content = replaceField(content, "CODE", invitation.getCode());
        
        if (invitation.getFoire() != null) {
            content = replaceField(content, "FOIRE_NAME", invitation.getFoire().getName());
        } else {
            content = replaceField(content, "FOIRE_NAME", "");
        }

        return content;
    }

    private String replaceField(String content, String fieldName, String value) {
        if (value == null) {
            value = "";
        }
        
        Pattern pattern = Pattern.compile("\\{\\{\\s*" + fieldName + "\\s*\\}\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.replaceAll(value);
    }

    private EmailTemplateDTO convertToDTO(EmailTemplate template) {
        List<EmailTemplateImageDTO> images = emailTemplateImageRepository
                .findByEmailTemplateIdOrderByImageOrderAsc(template.getId())
                .stream()
                .map(this::convertImageToDTO)
                .collect(Collectors.toList());

        return EmailTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .subject(template.getSubject())
                .htmlContent(template.getHtmlContent())
                .templateConfig(template.getTemplateConfig())
                .isActive(template.getIsActive())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .images(images)
                .build();
    }

    private EmailTemplateImageDTO convertImageToDTO(EmailTemplateImage image) {
        return EmailTemplateImageDTO.builder()
                .id(image.getId())
                .templateId(image.getEmailTemplate().getId())
                .imageUrl(image.getImageUrl())
                .imageName(image.getImageName())
                .imageOrder(image.getImageOrder())
                .createdAt(image.getCreatedAt())
                .build();
    }

    public void sendEmailWithTemplate(Long templateId, String recipientEmail, String nom, String date, String heure, String code, String foireName) throws MessagingException {
        
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        String processedSubject = template.getSubject()
                .replace("{{NOM}}", nom != null ? nom : "")
                .replace("{{EMAIL}}", recipientEmail != null ? recipientEmail : "")
                .replace("{{DATE}}", date != null ? date : "")
                .replace("{{HEURE}}", heure != null ? heure : "")
                .replace("{{CODE}}", code != null ? code : "")
                .replace("{{FOIRE_NAME}}", foireName != null ? foireName : "");

        String processedContent = template.getHtmlContent()
                .replace("{{NOM}}", nom != null ? nom : "")
                .replace("{{EMAIL}}", recipientEmail != null ? recipientEmail : "")
                .replace("{{DATE}}", date != null ? date : "")
                .replace("{{HEURE}}", heure != null ? heure : "")
                .replace("{{CODE}}", code != null ? code : "")
                .replace("{{FOIRE_NAME}}", foireName != null ? foireName : "");

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailUsername);
        helper.setTo(recipientEmail);
        helper.setSubject(processedSubject);
        helper.setText(processedContent, true);

        mailSender.send(message);
    }

    public void sendBulkEmails(List<String> recipients, String templateKey, Map<String, String> variables) {
        EmailTemplate template = emailTemplateRepository.findByName(templateKey)
                .orElseThrow(() -> new RuntimeException("Template not found with name: " + templateKey));

        for (String recipient : recipients) {
            try {
                String processedSubject = replaceVariables(template.getSubject(), variables);
                String processedContent = replaceVariables(template.getHtmlContent(), variables);

                sendEmail(recipient, processedSubject, processedContent);
            } catch (Exception e) {
                System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
            }
        }
    }

    private void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(mailUsername);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        mailSender.send(message);
    }

    private String replaceVariables(String content, Map<String, String> variables) {
        if (content == null || variables == null) {
            return content;
        }

        String result = content;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String value = entry.getValue() != null ? entry.getValue() : "";
            Pattern pattern = Pattern.compile("\\{\\{\\s*" + Pattern.quote(entry.getKey()) + "\\s*\\}\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(result);
            result = matcher.replaceAll(value);
        }
        return result;
    }
}
