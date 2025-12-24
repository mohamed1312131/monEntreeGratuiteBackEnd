package org.example.monentregratuit.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.example.monentregratuit.entity.EmailTemplate;
import org.example.monentregratuit.entity.NewsletterSubscriber;
import org.example.monentregratuit.repo.EmailTemplateRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsletterEmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateRepository emailTemplateRepository;
    private final NewsletterSubscriberService subscriberService;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public void sendNewsletterToSubscriber(Long templateId, NewsletterSubscriber subscriber) throws MessagingException {
        EmailTemplate template = emailTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + templateId));

        String processedSubject = replacePlaceholders(template.getSubject(), subscriber);
        String processedContent = replacePlaceholders(template.getHtmlContent(), subscriber);
        
        // Add unsubscribe footer
        processedContent = addUnsubscribeFooter(processedContent, subscriber.getUnsubscribeToken());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(mailUsername);
        helper.setTo(subscriber.getEmail());
        helper.setSubject(processedSubject);
        helper.setText(processedContent, true);

        // Add List-Unsubscribe headers for Gmail/Outlook one-click unsubscribe
        String unsubscribeUrl = frontendUrl + "/unsubscribe/" + subscriber.getUnsubscribeToken();
        message.addHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">");
        message.addHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");

        mailSender.send(message);

        // Record email sent
        subscriberService.recordEmailSent(subscriber.getId());
    }

    @Transactional
    public void sendBulkNewsletter(Long templateId, List<NewsletterSubscriber> subscribers) {
        int successCount = 0;
        int failureCount = 0;

        for (NewsletterSubscriber subscriber : subscribers) {
            try {
                sendNewsletterToSubscriber(templateId, subscriber);
                successCount++;
            } catch (Exception e) {
                failureCount++;
                System.err.println("Failed to send email to " + subscriber.getEmail() + ": " + e.getMessage());
                
                // Record bounce if it's a delivery failure
                if (e.getMessage() != null && e.getMessage().contains("bounce")) {
                    subscriberService.recordEmailBounce(subscriber.getEmail());
                }
            }
        }

        System.out.println("Newsletter sent: " + successCount + " successful, " + failureCount + " failed");
    }

    private String replacePlaceholders(String content, NewsletterSubscriber subscriber) {
        if (content == null) return "";

        return content
                .replace("{{NOM}}", subscriber.getName() != null ? subscriber.getName() : "")
                .replace("{{EMAIL}}", subscriber.getEmail() != null ? subscriber.getEmail() : "")
                .replace("{{NAME}}", subscriber.getName() != null ? subscriber.getName() : "");
    }

    private String addUnsubscribeFooter(String htmlContent, String unsubscribeToken) {
        String unsubscribeUrl = frontendUrl + "/unsubscribe/" + unsubscribeToken;
        
        String footer = 
            "<div style=\"margin-top: 40px; padding: 20px; background-color: #f5f5f5; border-top: 1px solid #e0e0e0; text-align: center; font-family: Arial, sans-serif;\">" +
            "  <p style=\"font-size: 12px; color: #666; margin: 0 0 10px 0;\">You're receiving this email because you subscribed to our newsletter.</p>" +
            "  <p style=\"font-size: 12px; color: #666; margin: 0;\">" +
            "    <a href=\"" + unsubscribeUrl + "\" style=\"color: #1976d2; text-decoration: none;\">Unsubscribe from future emails</a>" +
            "  </p>" +
            "</div>";

        // Try to insert before closing body tag, otherwise append
        if (htmlContent.contains("</body>")) {
            return htmlContent.replace("</body>", footer + "</body>");
        } else {
            return htmlContent + footer;
        }
    }
}
