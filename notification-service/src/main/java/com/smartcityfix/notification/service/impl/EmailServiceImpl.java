package com.smartcityfix.notification.service.impl;

import com.smartcityfix.notification.dto.NotificationRequest;
import com.smartcityfix.notification.model.EmailTemplate;
import com.smartcityfix.notification.model.Notification;
import com.smartcityfix.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(Notification notification) {
        try {
            // In a real application, you would fetch the user's email from the User Service
            // For now, we'll use a placeholder
            String userEmail = notification.getUserId() + "@example.com";

            sendEmail(userEmail, notification.getTitle(), notification.getMessage());
            log.info("Email sent to user: {}", notification.getUserId());
        } catch (Exception e) {
            log.error("Error sending email to user: {}", notification.getUserId(), e);
            throw e;
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
            throw e;
        }
    }

    @Override
    public String processTemplate(EmailTemplate template, NotificationRequest request) {
        String templateContent = template.getTemplate();

        // Replace placeholders with actual values
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("{{title}}", request.getTitle());
        placeholders.put("{{message}}", request.getMessage());
        placeholders.put("{{userId}}", request.getUserId().toString());
        if (request.getReferenceId() != null) {
            placeholders.put("{{referenceId}}", request.getReferenceId().toString());
        }

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            templateContent = templateContent.replace(entry.getKey(), entry.getValue());
        }

        return templateContent;
    }
}