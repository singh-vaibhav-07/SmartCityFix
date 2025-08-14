package com.smartcityfix.notification.controller;

import com.smartcityfix.common.dto.ApiResponse;
import com.smartcityfix.notification.dto.*;
import com.smartcityfix.notification.model.NotificationType;
import com.smartcityfix.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create notification", description = "Creates a new notification")
    public ResponseEntity<ApiResponse<NotificationResponse>> createNotification(@Valid @RequestBody NotificationRequest request) {
        log.info("Received request to create notification for user: {}", request.getUserId());
        NotificationResponse response = notificationService.createNotification(request);

        if (response == null) {
            return ResponseEntity.ok(ApiResponse.successMessage("Notification not created due to user preferences"));
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Notification created successfully", response));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create bulk notifications", description = "Creates notifications for multiple users")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> createBulkNotifications(@Valid @RequestBody BulkNotificationRequest request) {
        log.info("Received request to create bulk notifications for {} users", request.getUserIds().size());
        List<NotificationResponse> responses = notificationService.createBulkNotifications(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bulk notifications created successfully", responses));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID", description = "Returns notification details for the given ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotificationById(@PathVariable UUID id) {
        log.info("Fetching notification with id: {}", id);
        NotificationResponse response = notificationService.getNotificationById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user notifications", description = "Returns notifications for the specified user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUserNotifications(
            @PathVariable UUID userId,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching notifications for user: {}, read: {}, page: {}, size: {}", userId, read, page, size);
        Page<NotificationResponse> response = notificationService.getUserNotifications(userId, read, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "Mark notification as read", description = "Marks a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(@PathVariable UUID id) {
        log.info("Marking notification as read: {}", id);
        NotificationResponse response = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", response));
    }

    @PutMapping("/user/{userId}/read-all")
    @Operation(summary = "Mark all notifications as read", description = "Marks all notifications as read for a user")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable UUID userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.successNoData("All notifications marked as read"));
    }

    @GetMapping("/user/{userId}/unread-count")
    @Operation(summary = "Count unread notifications", description = "Returns the count of unread notifications for a user")
    public ResponseEntity<ApiResponse<Long>> countUnreadNotifications(@PathVariable UUID userId) {
        log.info("Counting unread notifications for user: {}", userId);
        long count = notificationService.countUnreadNotifications(userId);
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/preferences/{userId}")
    @Operation(summary = "Get notification preferences", description = "Returns notification preferences for a user")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getNotificationPreferences(@PathVariable UUID userId) {
        log.info("Fetching notification preferences for user: {}", userId);
        NotificationPreferenceResponse response = notificationService.getNotificationPreferences(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences", description = "Updates notification preferences for a user")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferenceRequest request) {
        log.info("Updating notification preferences for user: {}", request.getUserId());
        NotificationPreferenceResponse response = notificationService.updateNotificationPreferences(request);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated successfully", response));
    }

    @PostMapping("/templates")
    @Operation(summary = "Create email template", description = "Creates a new email template")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> createEmailTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        log.info("Creating email template for type: {}", request.getType());
        EmailTemplateResponse response = notificationService.createEmailTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Email template created successfully", response));
    }

    @GetMapping("/templates/{type}")
    @Operation(summary = "Get email template", description = "Returns email template for the specified type")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> getEmailTemplate(@PathVariable NotificationType type) {
        log.info("Fetching email template for type: {}", type);
        EmailTemplateResponse response = notificationService.getEmailTemplate(type);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/templates")
    @Operation(summary = "Update email template", description = "Updates an existing email template")
    public ResponseEntity<ApiResponse<EmailTemplateResponse>> updateEmailTemplate(@Valid @RequestBody EmailTemplateRequest request) {
        log.info("Updating email template for type: {}", request.getType());
        EmailTemplateResponse response = notificationService.updateEmailTemplate(request);
        return ResponseEntity.ok(ApiResponse.success("Email template updated successfully", response));
    }

    @GetMapping("/templates")
    @Operation(summary = "Get all email templates", description = "Returns all email templates")
    public ResponseEntity<ApiResponse<List<EmailTemplateResponse>>> getAllEmailTemplates() {
        log.info("Fetching all email templates");
        List<EmailTemplateResponse> response = notificationService.getAllEmailTemplates();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}