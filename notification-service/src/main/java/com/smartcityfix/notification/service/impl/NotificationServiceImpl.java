package com.smartcityfix.notification.service.impl;

import com.smartcityfix.common.exception.ResourceNotFoundException;
import com.smartcityfix.notification.dto.*;
import com.smartcityfix.notification.model.*;
import com.smartcityfix.notification.repository.EmailTemplateRepository;
import com.smartcityfix.notification.repository.NotificationPreferenceRepository;
import com.smartcityfix.notification.repository.NotificationRepository;
import com.smartcityfix.notification.service.EmailService;
import com.smartcityfix.notification.service.NotificationService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailTemplateRepository templateRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for user: {}", request.getUserId());

        try {
            // Check if user has preferences
            NotificationPreference preferences = preferenceRepository.findById(request.getUserId())
                    .orElseGet(() -> createDefaultPreferences(request.getUserId()));

            // Check if user wants this type of notification on this channel
            if (!shouldSendNotification(preferences, request.getType(), request.getChannel())) {
                log.info("User {} has disabled {} notifications on {} channel",
                        request.getUserId(), request.getType(), request.getChannel());
                return null;
            }

            Notification notification = Notification.builder()
                    .userId(request.getUserId())
                    .title(request.getTitle())
                    .message(request.getMessage())
                    .type(request.getType())
                    .referenceId(request.getReferenceId())
                    .channel(request.getChannel())
                    .read(false)
                    .sent(false)
                    .build();

            Notification savedNotification = notificationRepository.save(notification);
            log.info("Notification created with id: {}", savedNotification.getId());

            return mapToNotificationResponse(savedNotification);
        } catch (Exception e) {
            log.error("Error creating notification", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public List<NotificationResponse> createBulkNotifications(BulkNotificationRequest request) {
        log.info("Creating bulk notifications for {} users", request.getUserIds().size());

        try {
            List<Notification> notifications = new ArrayList<>();

            for (UUID userId : request.getUserIds()) {
                // Check if user has preferences
                NotificationPreference preferences = preferenceRepository.findById(userId)
                        .orElseGet(() -> createDefaultPreferences(userId));

                // Check if user wants this type of notification on this channel
                if (shouldSendNotification(preferences, request.getType(), request.getChannel())) {
                    Notification notification = Notification.builder()
                            .userId(userId)
                            .title(request.getTitle())
                            .message(request.getMessage())
                            .type(request.getType())
                            .referenceId(request.getReferenceId())
                            .channel(request.getChannel())
                            .read(false)
                            .sent(false)
                            .build();

                    notifications.add(notification);
                }
            }

            List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
            log.info("Created {} bulk notifications", savedNotifications.size());

            return savedNotifications.stream()
                    .map(this::mapToNotificationResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error creating bulk notifications", e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID id) {
        log.info("Fetching notification with id: {}", id);

        try {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

            return mapToNotificationResponse(notification);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching notification with id: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Boolean read, int page, int size) {
        log.info("Fetching notifications for user: {}, read: {}, page: {}, size: {}", userId, read, page, size);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            Page<Notification> notifications;

            if (read != null) {
                notifications = notificationRepository.findByUserIdAndRead(userId, read, pageable);
            } else {
                notifications = notificationRepository.findByUserId(userId, pageable);
            }

            return notifications.map(this::mapToNotificationResponse);
        } catch (Exception e) {
            log.error("Error fetching notifications for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id) {
        log.info("Marking notification as read: {}", id);

        try {
            Notification notification = notificationRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

            notification.setRead(true);
            Notification updatedNotification = notificationRepository.save(notification);

            return mapToNotificationResponse(updatedNotification);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error marking notification as read: {}", id, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        try {
            List<Notification> unreadNotifications = notificationRepository.findByUserIdAndRead(userId, false, Pageable.unpaged()).getContent();

            for (Notification notification : unreadNotifications) {
                notification.setRead(true);
            }

            notificationRepository.saveAll(unreadNotifications);
            log.info("Marked {} notifications as read for user: {}", unreadNotifications.size(), userId);
        } catch (Exception e) {
            log.error("Error marking all notifications as read for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotifications(UUID userId) {
        log.info("Counting unread notifications for user: {}", userId);

        try {
            return notificationRepository.countByUserIdAndRead(userId, false);
        } catch (Exception e) {
            log.error("Error counting unread notifications for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    @Scheduled(fixedRate = 60000) // Run every minute
    @Transactional
    @CircuitBreaker(name = "notificationService", fallbackMethod = "sendPendingNotificationsFallback")
    @Retry(name = "notificationService")
    public void sendPendingNotifications() {
        log.info("Sending pending notifications");

        try {
            // Process email notifications
            List<Notification> pendingEmails = notificationRepository.findAll().stream()
                    .filter(n -> n.getChannel() == NotificationChannel.EMAIL && !n.isSent())
                    .collect(Collectors.toList());

            for (Notification notification : pendingEmails) {
                try {
                    emailService.sendEmail(notification);

                    notification.setSent(true);
                    notification.setSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);

                    log.info("Sent email notification: {}", notification.getId());
                } catch (Exception e) {
                    log.error("Error sending email notification: {}", notification.getId(), e);
                    // Continue with next notification
                }
            }

            // Process SMS notifications (would integrate with SMS provider)
            List<Notification> pendingSms = notificationRepository.findAll().stream()
                    .filter(n -> n.getChannel() == NotificationChannel.SMS && !n.isSent())
                    .collect(Collectors.toList());

            for (Notification notification : pendingSms) {
                try {
                    // Simulate sending SMS
                    log.info("Simulating SMS sending for notification: {}", notification.getId());

                    notification.setSent(true);
                    notification.setSentAt(LocalDateTime.now());
                    notificationRepository.save(notification);

                    log.info("Sent SMS notification: {}", notification.getId());
                } catch (Exception e) {
                    log.error("Error sending SMS notification: {}", notification.getId(), e);
                    // Continue with next notification
                }
            }

            // In-app notifications are considered sent immediately
            List<Notification> pendingInApp = notificationRepository.findAll().stream()
                    .filter(n -> n.getChannel() == NotificationChannel.IN_APP && !n.isSent())
                    .collect(Collectors.toList());

            for (Notification notification : pendingInApp) {
                notification.setSent(true);
                notification.setSentAt(LocalDateTime.now());
            }

            if (!pendingInApp.isEmpty()) {
                notificationRepository.saveAll(pendingInApp);
                log.info("Marked {} in-app notifications as sent", pendingInApp.size());
            }

            log.info("Processed {} pending notifications", pendingEmails.size() + pendingSms.size() + pendingInApp.size());
        } catch (Exception e) {
            log.error("Error sending pending notifications", e);
            throw e;
        }
    }

    public void sendPendingNotificationsFallback(Exception e) {
        log.error("Fallback: Error sending pending notifications", e);
        // Could implement retry logic or alert monitoring system
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getNotificationPreferences(UUID userId) {
        log.info("Fetching notification preferences for user: {}", userId);

        try {
            NotificationPreference preferences = preferenceRepository.findById(userId)
                    .orElseGet(() -> createDefaultPreferences(userId));

            return mapToPreferenceResponse(preferences);
        } catch (Exception e) {
            log.error("Error fetching notification preferences for user: {}", userId, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public NotificationPreferenceResponse updateNotificationPreferences(NotificationPreferenceRequest request) {
        log.info("Updating notification preferences for user: {}", request.getUserId());

        try {
            NotificationPreference preferences = preferenceRepository.findById(request.getUserId())
                    .orElseGet(() -> createDefaultPreferences(request.getUserId()));

            if (request.getEnabledChannels() != null) {
                preferences.setEnabledChannels(request.getEnabledChannels());
            }

            if (request.getEnabledTypes() != null) {
                preferences.setEnabledTypes(request.getEnabledTypes());
            }

            if (request.getEmailEnabled() != null) {
                preferences.setEmailEnabled(request.getEmailEnabled());
            }

            if (request.getSmsEnabled() != null) {
                preferences.setSmsEnabled(request.getSmsEnabled());
            }

            if (request.getInAppEnabled() != null) {
                preferences.setInAppEnabled(request.getInAppEnabled());
            }

            NotificationPreference updatedPreferences = preferenceRepository.save(preferences);
            log.info("Updated notification preferences for user: {}", request.getUserId());

            return mapToPreferenceResponse(updatedPreferences);
        } catch (Exception e) {
            log.error("Error updating notification preferences for user: {}", request.getUserId(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public EmailTemplateResponse createEmailTemplate(EmailTemplateRequest request) {
        log.info("Creating email template for type: {}", request.getType());

        try {
            // Check if template already exists
            templateRepository.findById(request.getType()).ifPresent(t -> {
                throw new IllegalArgumentException("Template for type " + request.getType() + " already exists");
            });

            EmailTemplate template = EmailTemplate.builder()
                    .type(request.getType())
                    .subject(request.getSubject())
                    .template(request.getTemplate())
                    .build();

            EmailTemplate savedTemplate = templateRepository.save(template);
            log.info("Created email template for type: {}", request.getType());

            return mapToTemplateResponse(savedTemplate);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating email template for type: {}", request.getType(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EmailTemplateResponse getEmailTemplate(NotificationType type) {
        log.info("Fetching email template for type: {}", type);

        try {
            EmailTemplate template = templateRepository.findById(type)
                    .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "type", type));

            return mapToTemplateResponse(template);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching email template for type: {}", type, e);
            throw e;
        }
    }

    @Override
    @Transactional
    public EmailTemplateResponse updateEmailTemplate(EmailTemplateRequest request) {
        log.info("Updating email template for type: {}", request.getType());

        try {
            EmailTemplate template = templateRepository.findById(request.getType())
                    .orElseThrow(() -> new ResourceNotFoundException("EmailTemplate", "type", request.getType()));

            template.setSubject(request.getSubject());
            template.setTemplate(request.getTemplate());

            EmailTemplate updatedTemplate = templateRepository.save(template);
            log.info("Updated email template for type: {}", request.getType());

            return mapToTemplateResponse(updatedTemplate);
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating email template for type: {}", request.getType(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getAllEmailTemplates() {
        log.info("Fetching all email templates");

        try {
            List<EmailTemplate> templates = templateRepository.findAll();

            return templates.stream()
                    .map(this::mapToTemplateResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching all email templates", e);
            throw e;
        }
    }

    private NotificationPreference createDefaultPreferences(UUID userId) {
        log.info("Creating default notification preferences for user: {}", userId);

        Set<NotificationChannel> defaultChannels = new HashSet<>();
        defaultChannels.add(NotificationChannel.EMAIL);
        defaultChannels.add(NotificationChannel.IN_APP);

        Set<NotificationType> defaultTypes = new HashSet<>();
        defaultTypes.add(NotificationType.COMPLAINT_CREATED);
        defaultTypes.add(NotificationType.COMPLAINT_ASSIGNED);
        defaultTypes.add(NotificationType.COMPLAINT_STATUS_UPDATED);
        defaultTypes.add(NotificationType.COMPLAINT_RESOLVED);
        defaultTypes.add(NotificationType.ACCOUNT_CREATED);

        NotificationPreference preferences = NotificationPreference.builder()
                .userId(userId)
                .enabledChannels(defaultChannels)
                .enabledTypes(defaultTypes)
                .emailEnabled(true)
                .smsEnabled(false)
                .inAppEnabled(true)
                .build();

        return preferenceRepository.save(preferences);
    }

    private boolean shouldSendNotification(NotificationPreference preferences, NotificationType type, NotificationChannel channel) {
        // Check if the type is enabled
        if (!preferences.getEnabledTypes().contains(type)) {
            return false;
        }

        // Check if the channel is enabled
        if (!preferences.getEnabledChannels().contains(channel)) {
            return false;
        }

        // Check specific channel settings
        switch (channel) {
            case EMAIL:
                return preferences.isEmailEnabled();
            case SMS:
                return preferences.isSmsEnabled();
            case IN_APP:
                return preferences.isInAppEnabled();
            default:
                return false;
        }
    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .referenceId(notification.getReferenceId())
                .channel(notification.getChannel())
                .read(notification.isRead())
                .sent(notification.isSent())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    private NotificationPreferenceResponse mapToPreferenceResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .userId(preference.getUserId())
                .enabledChannels(preference.getEnabledChannels())
                .enabledTypes(preference.getEnabledTypes())
                .emailEnabled(preference.isEmailEnabled())
                .smsEnabled(preference.isSmsEnabled())
                .inAppEnabled(preference.isInAppEnabled())
                .createdAt(preference.getCreatedAt())
                .updatedAt(preference.getUpdatedAt())
                .build();
    }

    private EmailTemplateResponse mapToTemplateResponse(EmailTemplate template) {
        return EmailTemplateResponse.builder()
                .type(template.getType())
                .subject(template.getSubject())
                .template(template.getTemplate())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}