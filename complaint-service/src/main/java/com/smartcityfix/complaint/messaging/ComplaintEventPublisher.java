package com.smartcityfix.complaint.messaging;

import com.smartcityfix.common.event.ComplaintCreatedEvent;
import com.smartcityfix.complaint.model.Complaint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplaintEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.complaint-created}")
    private String complaintCreatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-assigned}")
    private String complaintAssignedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-status-updated}")
    private String complaintStatusUpdatedRoutingKey;

    @Value("${app.rabbitmq.routing-key.complaint-resolved}")
    private String complaintResolvedRoutingKey;

    public void publishComplaintCreatedEvent(Complaint complaint) {
        try {
            log.info("Publishing ComplaintCreatedEvent for complaint: {}", complaint.getId());

            // Use constructor instead of builder
            ComplaintCreatedEvent.LocationDto locationDto = new ComplaintCreatedEvent.LocationDto(
                    complaint.getLocation().getLatitude(),
                    complaint.getLocation().getLongitude(),
                    complaint.getLocation().getAddress()
            );

            ComplaintCreatedEvent event = ComplaintCreatedEvent.builder()
                    .complaintId(complaint.getId())
                    .category(complaint.getCategory().name())
                    .location(locationDto)
                    .reportedBy(complaint.getReportedBy())
                    .build();

            rabbitTemplate.convertAndSend(exchange, complaintCreatedRoutingKey, event);
            log.info("ComplaintCreatedEvent published successfully");
        } catch (Exception e) {
            log.error("Failed to publish ComplaintCreatedEvent", e);
            throw e;
        }
    }

    public void publishComplaintAssignedEvent(Complaint complaint) {
        // Implementation for complaint assigned event
        log.info("Publishing ComplaintAssignedEvent for complaint: {}", complaint.getId());
        // Create and publish event
    }

    public void publishComplaintStatusUpdatedEvent(Complaint complaint, String oldStatus, String newStatus) {
        // Implementation for complaint status updated event
        log.info("Publishing ComplaintStatusUpdatedEvent for complaint: {}", complaint.getId());
        // Create and publish event
    }

    public void publishComplaintResolvedEvent(Complaint complaint, UUID resolvedBy, String resolutionNotes) {
        // Implementation for complaint resolved event
        log.info("Publishing ComplaintResolvedEvent for complaint: {}", complaint.getId());
        // Create and publish event
    }
}