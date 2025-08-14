package com.smartcityfix.notification.service;

import com.smartcityfix.notification.dto.*;
import com.smartcityfix.notification.model.NotificationType;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface NotificationService {

    NotificationResponse createNotification(NotificationRequest request);

    List<NotificationResponse> createBulkNotifications(BulkNotificationRequest request);

    NotificationResponse getNotificationById(UUID id);

    Page<NotificationResponse> getUserNotifications(UUID userId, Boolean read, int page, int size);

    NotificationResponse markAsRead(UUID id);

    void markAllAsRead(UUID userId);

    long countUnreadNotifications(UUID userId);

    void sendPendingNotifications();

    NotificationPreferenceResponse getNotificationPreferences(UUID userId);

    NotificationPreferenceResponse updateNotificationPreferences(NotificationPreferenceRequest request);

    EmailTemplateResponse createEmailTemplate(EmailTemplateRequest request);

    EmailTemplateResponse getEmailTemplate(NotificationType type);

    EmailTemplateResponse updateEmailTemplate(EmailTemplateRequest request);

    List<EmailTemplateResponse> getAllEmailTemplates();
}