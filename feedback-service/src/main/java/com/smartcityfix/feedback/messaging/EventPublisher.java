package com.smartcityfix.feedback.messaging;

import com.smartcityfix.common.event.FeedbackCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.routing-key.feedback-created}")
    private String feedbackCreatedRoutingKey;

    public void publishFeedbackCreatedEvent(UUID feedbackId, UUID complaintId, UUID userId, UUID departmentId, Integer rating) {
        try {
            FeedbackCreatedEvent event = new FeedbackCreatedEvent();
            event.setFeedbackId(feedbackId);
            event.setComplaintId(complaintId);
            event.setUserId(userId);
            event.setDepartmentId(departmentId);
            event.setRating(rating);

            rabbitTemplate.convertAndSend(exchange, feedbackCreatedRoutingKey, event);
            log.info("Published FeedbackCreatedEvent for feedback: {}", feedbackId);
        } catch (Exception e) {
            log.error("Error publishing FeedbackCreatedEvent", e);
            throw e;
        }
    }
}