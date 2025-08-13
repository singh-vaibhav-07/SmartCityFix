package com.smartcityfix.department.messaging;

import com.smartcityfix.common.event.ComplaintAssignedEvent;
import com.smartcityfix.common.event.ComplaintCreatedEvent;
import com.smartcityfix.common.event.ComplaintResolvedEvent;
import com.smartcityfix.department.dto.DepartmentResponse;
import com.smartcityfix.department.dto.LocationDto;
import com.smartcityfix.department.dto.RoutingRequest;
import com.smartcityfix.department.dto.RoutingResponse;
import com.smartcityfix.department.model.ComplaintCategory;
import com.smartcityfix.department.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ComplaintEventListener {

    private final DepartmentService departmentService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.complaint-assigned}")
    private String complaintAssignedRoutingKey;

    @RabbitListener(queues = "${app.rabbitmq.queue.complaint-created}")
    public void handleComplaintCreatedEvent(ComplaintCreatedEvent event) {
        log.info("Received ComplaintCreatedEvent for complaint: {}", event.getComplaintId());

        try {
            // Convert category string to enum
            ComplaintCategory category = ComplaintCategory.valueOf(event.getCategory());

            // Create routing request with corrected field access
            LocationDto locationDto = LocationDto.builder()
                    .latitude(event.getLocation().getLat())  // Changed from getLatitude to getLat
                    .longitude(event.getLocation().getLon()) // Changed from getLongitude to getLon
                    .address(event.getLocation().getAddress())
                    .build();

            RoutingRequest routingRequest = RoutingRequest.builder()
                    .category(category)
                    .location(locationDto)
                    .build();

            // Find best department
            RoutingResponse routingResponse = departmentService.routeComplaint(routingRequest);

            if (routingResponse != null) {
                log.info("Routed complaint {} to department {}",
                        event.getComplaintId(), routingResponse.getDepartmentId());

                // Publish complaint assigned event - fixed type mismatch
                publishComplaintAssignedEvent(event.getComplaintId().toString(), routingResponse.getDepartmentId());
            } else {
                log.warn("Could not find suitable department for complaint {}", event.getComplaintId());
                // Handle fallback logic - could assign to a default department or escalate
            }
        } catch (Exception e) {
            log.error("Error processing ComplaintCreatedEvent", e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.complaint-resolved}")
    public void handleComplaintResolvedEvent(ComplaintResolvedEvent event) {
        log.info("Received ComplaintResolvedEvent for complaint: {}", event.getComplaintId());

        try {
            // Decrement workload for the department
            if (event.getDepartmentId() != null) {
                departmentService.decrementWorkload(event.getDepartmentId());
                log.info("Decremented workload for department {}", event.getDepartmentId());
            }
        } catch (Exception e) {
            log.error("Error processing ComplaintResolvedEvent", e);
        }
    }

    private void publishComplaintAssignedEvent(String complaintIdStr, UUID departmentId) {
        try {
            UUID complaintId = UUID.fromString(complaintIdStr);

            // Get department name
            DepartmentResponse department = departmentService.getDepartmentById(departmentId);

            ComplaintAssignedEvent event = new ComplaintAssignedEvent(
                    complaintId,
                    departmentId,
                    department.getName()
            );

            log.info("Publishing ComplaintAssignedEvent for complaint: {}", complaintId);
            rabbitTemplate.convertAndSend(exchange, complaintAssignedRoutingKey, event);
        } catch (Exception e) {
            log.error("Error publishing ComplaintAssignedEvent", e);
        }
    }
}