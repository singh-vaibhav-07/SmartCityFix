package com.smartcityfix.notification.messaging;

import com.smartcityfix.common.event.ComplaintAssignedEvent;
import com.smartcityfix.common.event.ComplaintCreatedEvent;
import com.smartcityfix.common.event.ComplaintResolvedEvent;
import com.smartcityfix.common.event.UserRegisteredEvent;
import com.smartcityfix.notification.dto.NotificationRequest;
import com.smartcityfix.notification.model.NotificationChannel;
import com.smartcityfix.notification.model.NotificationType;
import com.smartcityfix.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbitmq.queue.user-registered}")
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for user: {}", event.getUserId());

        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .title("Welcome to SmartCityFix")
                    .message("Thank you for registering with SmartCityFix. You can now report and track city infrastructure issues.")
                    .type(NotificationType.ACCOUNT_CREATED)
                    .channel(NotificationChannel.EMAIL)
                    .build();

            notificationService.createNotification(request);
            log.info("Created welcome notification for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error processing UserRegisteredEvent", e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.complaint-created}")
    public void handleComplaintCreatedEvent(ComplaintCreatedEvent event) {
        log.info("Received ComplaintCreatedEvent for complaint: {}", event.getComplaintId());

        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getReportedBy())
                    .title("Complaint Submitted Successfully")
                    .message("Your complaint has been submitted successfully. We will keep you updated on its progress.")
                    .type(NotificationType.COMPLAINT_CREATED)
                    .referenceId(event.getComplaintId())
                    .channel(NotificationChannel.EMAIL)
                    .build();

            notificationService.createNotification(request);

            // Also create in-app notification
            NotificationRequest inAppRequest = NotificationRequest.builder()
                    .userId(event.getReportedBy())
                    .title("Complaint Submitted")
                    .message("Your complaint has been submitted successfully.")
                    .type(NotificationType.COMPLAINT_CREATED)
                    .referenceId(event.getComplaintId())
                    .channel(NotificationChannel.IN_APP)
                    .build();

            notificationService.createNotification(inAppRequest);

            log.info("Created complaint submission notifications for user: {}", event.getReportedBy());
        } catch (Exception e) {
            log.error("Error processing ComplaintCreatedEvent", e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.complaint-assigned}")
    public void handleComplaintAssignedEvent(ComplaintAssignedEvent event) {
        log.info("Received ComplaintAssignedEvent for complaint: {}", event.getComplaintId());

        try {
            // Get the user ID from the complaint service
            // For now, we'll assume we have it in the event
            UUID userId = event.getComplaintId(); // This should be the reporter's ID

            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .title("Complaint Assigned")
                    .message("Your complaint has been assigned to " + event.getDepartmentName() + " department.")
                    .type(NotificationType.COMPLAINT_ASSIGNED)
                    .referenceId(event.getComplaintId())
                    .channel(NotificationChannel.EMAIL)
                    .build();

            notificationService.createNotification(request);

            // Also create in-app notification
            NotificationRequest inAppRequest = NotificationRequest.builder()
                    .userId(userId)
                    .title("Complaint Assigned")
                    .message("Your complaint has been assigned to " + event.getDepartmentName() + " department.")
                    .type(NotificationType.COMPLAINT_ASSIGNED)
                    .referenceId(event.getComplaintId())
                    .channel(NotificationChannel.IN_APP)
                    .build();

            notificationService.createNotification(inAppRequest);

            log.info("Created complaint assignment notifications for user: {}", userId);
        } catch (Exception e) {
            log.error("Error processing ComplaintAssignedEvent", e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.complaint-resolved}")
    public void handleComplaintResolvedEvent(ComplaintResolvedEvent event) {
        log.info("Received ComplaintResolvedEvent for complaint: {}", event.getComplaintId());

        try {
            // Get the user ID from the complaint service
            // For now, we'll assume we have it in the event
            UUID userId = event.getComplaintId(); // This should be the reporter's ID

            NotificationRequest request = NotificationRequest.builder()
                    .userId(userId)
                    .title("Complaint Resolved")
                    .message("Your complaint has been resolved. Thank you for using SmartCityFix.")
                    .type(NotificationType.COMPLAINT_RESOLVED)
                    .referenceId(event.getComplaintId())
                    .channel(NotificationChannel.EMAIL)
                    .build();

            notificationService.createNotification(request);

            // Also create in-app notification
            NotificationRequest inAppRequest = NotificationRequest.builder()
                    .userId(userId)
                    .title("Complaint Resolved")
                    .message("Your complaint has been resolved. Thank you for using SmartCityFix.")
                    .type(NotificationType.COMPLAINT_RESOLVED)
                    .referenceId(event.getComplaintId())
                    .channel(NotificationChannel.IN_APP)
                    .build();

            notificationService.createNotification(inAppRequest);

            log.info("Created complaint resolution notifications for user: {}", userId);
        } catch (Exception e) {
            log.error("Error processing ComplaintResolvedEvent", e);
        }
    }
}