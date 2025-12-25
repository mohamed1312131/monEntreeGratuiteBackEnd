package org.example.monentregratuit.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private JavaMailSender mailSender;

    @GetMapping("/api/health")
    public String health() {
        return "OK";
    }

    @GetMapping("/test-email")
    public String testEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("test@example.com");
        message.setSubject("Test Email");
        message.setText("This is a test email from Spring Boot!");
        mailSender.send(message);
        return "Email sent!";
    }
}