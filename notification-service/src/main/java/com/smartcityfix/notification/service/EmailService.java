package com.smartcityfix.notification.service;

import com.smartcityfix.notification.dto.NotificationRequest;
import com.smartcityfix.notification.model.EmailTemplate;
import com.smartcityfix.notification.model.Notification;

public interface EmailService {

    void sendEmail(Notification notification);

    void sendEmail(String to, String subject, String body);

    String processTemplate(EmailTemplate template, NotificationRequest request);
}